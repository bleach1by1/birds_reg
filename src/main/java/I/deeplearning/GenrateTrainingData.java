package I.deeplearning;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import I.plugin.LogIn;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.plugin.PlugIn;

/**
 * genrate training data module 
 * @author zengweilin
 *
 */

public class GenrateTrainingData implements PlugIn{

	public static boolean JUDGE = false; 

	
	public static int FILTERVOXELNUM = 100;
	public static int MAXTHREAD = 50;
	public static int VALTOTRAIN = 10;
	
	public JFrame frame;
	public JTextField regionUrl,outUrl,trianField,valField;
	public Button regionChoose,outUrlChoose,ok;
	
	public void location() {
		frame = new JFrame("genrate training data");
		frame.setBounds(300, 400, 450, 210);
		frame.setLayout(null);
		
		int beginX = 30,beginY = 10,high = 20;
		JLabel regionLabel = new JLabel("region:");
		regionUrl = new JTextField(100);
		regionChoose = new Button("choose");
		regionLabel.setBounds(beginX, beginY, 80, high);
		regionUrl.setBounds(beginX+=85, beginY, 200, high);
		regionChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(regionLabel);
		frame.add(regionUrl);
		frame.add(regionChoose);
		
		beginX = 30; beginY += 30;
		JLabel proportionLabel = new JLabel("proportion:");
		JLabel trainLabel = new JLabel("train:");
		trianField = new JTextField(50);
		JLabel valLabel = new JLabel("val:");
		valField = new JTextField(50);
		proportionLabel.setBounds(beginX, beginY, 80, high);
		trainLabel.setBounds(beginX+=90, beginY, 40, high);
		trianField.setBounds(beginX+=40, beginY, 40, high);
		valLabel.setBounds(beginX+=70, beginY, 40, high);
		valField.setBounds(beginX+=40, beginY, 40, high);
		frame.add(proportionLabel);
		frame.add(trainLabel);
		frame.add(trianField);
		frame.add(valLabel);
		frame.add(valField);
		trianField.setText("9");
		valField.setText("1");
		
		
		beginX = 30; beginY += 30;
		JLabel outUrlLabel = new JLabel("outURL:");
		outUrl = new JTextField(100);
		outUrlChoose = new Button("choose");
		outUrlLabel.setBounds(beginX, beginY, 80, high);
		outUrl.setBounds(beginX+=85, beginY, 200, high);
		outUrlChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(outUrlLabel);
		frame.add(outUrl);
		frame.add(outUrlChoose);
		
		beginX = 130; beginY += 30;
		ok = new Button("ok");
		ok.setBounds(beginX, beginY, 40, high);
		frame.add(ok);
		
		frame.setVisible(true);
		event();
	}
	
	public void event() {
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				 frame.setVisible(false);
			}
		});
		
		outUrlChoose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				outUrl.setText(URL);
			}
		});
		
		regionChoose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				regionUrl.setText(URL);
			}
		});
		
		
		
		ok.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				if(!JUDGE) {
					try {
						JUDGE = true;
						new Thread(()->{
							String regionStr = regionUrl.getText();
							String outStr = outUrl.getText();
							String rawUrl = regionStr+"/raw", annUrl = regionStr+"/ann";
							int trainInt = Integer.valueOf(trianField.getText()),valInt = Integer.valueOf(valField.getText());
							VALTOTRAIN = trainInt/valInt+1;
							
							new File(outStr+"/raw").mkdirs();
							new File(outStr+"/ann").mkdirs();
							new File(outStr+"/seg").mkdirs();
							
							File tempRaw = new File(rawUrl);
							File trainTxt = new File(outStr+"/seg/train.txt");
							File valTxt = new File(outStr+"/seg/val.txt");
							File trainValTxt = new File(outStr+"/seg/trainval.txt");
							try {
								trainTxt.createNewFile();
								valTxt.createNewFile();
								trainValTxt.createNewFile();
								BufferedWriter trainBr = new BufferedWriter(new FileWriter(trainTxt));
								BufferedWriter valBr = new BufferedWriter(new FileWriter(valTxt));
								BufferedWriter trainValBr = new BufferedWriter(new FileWriter(trainValTxt));
								int numTemp = 0;
								for(File f:tempRaw.listFiles()) {
									String name = f.getName();
									ImagePlus rawImage = new ImagePlus(rawUrl+"/"+name);
									ImagePlus annImage = new ImagePlus(annUrl+"/"+name);
									ImageStack rawStack = rawImage.getImageStack(),annStack = annImage.getImageStack();
									int lenZ = rawImage.getStackSize();
									System.out.println(lenZ+name);
									
									int maxThread = 50,batch = (lenZ/maxThread)+(((lenZ%maxThread)>0)?1:0);
									for(int b=0;b<batch;b++) {
										int numThread = ((lenZ-b*maxThread)>maxThread)?maxThread:(lenZ-b*maxThread);
										CountDownLatch count = new CountDownLatch(numThread);
										for(int z=b*maxThread;z<b*maxThread+numThread;z++) {
											int zz = z;
											new Thread(()->{
												try {
													int moreThanNum = 0;
													for(int x=0;x<annImage.getWidth();x++) {
														for(int y=0;y<annImage.getHeight();y++) {
															int val = (int)annStack.getVoxel(x, y, zz);
															if(val>0) {
																moreThanNum++;
																if(moreThanNum>FILTERVOXELNUM) break;
															}
														}
														if(moreThanNum>FILTERVOXELNUM) break;
													}
													
													if(moreThanNum<=FILTERVOXELNUM) {
														count.countDown();
														return;
													}
													
													ImagePlus annOutImage = new ImagePlus(name.split("[.]")[0]+String.format("%04d", zz+1), annStack.getProcessor(zz+1));
													ImagePlus rawOutIamge = new ImagePlus(name.split("[.]")[0]+String.format("%04d", zz+1), rawStack.getProcessor(zz+1).convertToColorProcessor());
													new FileSaver(rawOutIamge).saveAsPng(outStr+"/raw/"+name.split("[.]")[0]+String.format("%04d", zz+1)+".png");
													new FileSaver(annOutImage).saveAsPng(outStr+"/ann/"+name.split("[.]")[0]+String.format("%04d", zz+1)+".png");
													if((zz+1)%VALTOTRAIN==0) {
														valBr.write(name.split("[.]")[0]+String.format("%04d", zz+1)+"\n");
													}else {
														trainBr.write(name.split("[.]")[0]+String.format("%04d", zz+1)+"\n");
													}
													trainValBr.write(name.split("[.]")[0]+String.format("%04d", zz+1)+"\n");
												} catch (Exception e2) {
													LogIn.error(e2.getMessage());
												}finally {
													count.countDown();
 												}
												count.countDown();
											}).start();
										}
										try {
											count.await();
										} catch (Exception e3) {
											LogIn.error(e3.getMessage());
										}
									}
								}
								trainBr.flush();trainBr.close();
								valBr.flush();valBr.close();
								trainValBr.flush();trainValBr.close();
							} catch (IOException e1) {
								IJ.log(e1.getMessage());
							}finally {
								JUDGE = false;
							}
							JUDGE = false;
						}).start();
					} catch (Exception e2) {
						IJ.log(e2.getMessage());
					}finally {
						JUDGE = false;
					}
				}else {
					JOptionPane.showMessageDialog(null, "this is running", "error",JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
	}
	
	public void run(String arg) {
		location();
	}
	
	public static void main(String[] args) {
		GenrateTrainingData g = new GenrateTrainingData();
		g.location();
		g.regionUrl.setText("I:\\han\\delete");
		g.outUrl.setText("I:\\han\\delete\\out");
	}
}
