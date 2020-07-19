package I.cellcounting;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import I.deeplearning.FuseDemoNewRight;
import I.plugin.GlobalValue;
import I.utils.FileCopyUtils;
import I.utils.ExcelTools;
import I.visualization.tools.GetBox3DExcel;
import I.visualization.tools.UpSampleTools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.PlugIn;

public class CaculRegionCounting implements PlugIn{

	public static int MAXTHREAD = 20;
	
	public static boolean JUDGE = false; 
	
	private JFrame frame;
	private JTextField imageField,excelField, annotationField, globalField, outField;
	private Button imageButton,annotationButton,excelButton,okButton,globalButton, outButton;
	
	private void location() {
		frame = new JFrame();
		frame.setBounds(300, 400, 450, 210);
		frame.setLayout(null);
		
		int beginX = 30,beginY = 10,high = 20;
		JLabel imageLabel = new JLabel("image url:");
		imageField = new JTextField(50);
		imageButton = new Button("choose");
		imageLabel.setBounds(beginX, beginY, 80, high);
		imageField.setBounds(beginX+=85, beginY, 200, high);
		imageButton.setBounds(beginX+=210, beginY, 60, high);
		frame.add(imageLabel);
		frame.add(imageField);
		frame.add(imageButton);
		
		beginX = 30;beginY+=30;
		JLabel annotationLabel = new JLabel("annotation:");
		annotationField = new JTextField(50);
		annotationButton = new Button("choose");
		annotationLabel.setBounds(beginX, beginY, 80, high);
		annotationField.setBounds(beginX+=85, beginY, 200, high);
		annotationButton.setBounds(beginX+=210, beginY, 60, high);
		frame.add(annotationLabel);
		frame.add(annotationField);
		frame.add(annotationButton);
		
		beginX = 30;beginY+=30;
		JLabel excelLabel = new JLabel("excel:");
		excelField = new JTextField(50);
		excelField.setText("Default all region");
		excelButton = new Button("choose");
		excelLabel.setBounds(beginX, beginY, 80, high);
		excelField.setBounds(beginX+=85, beginY, 200, high);
		excelButton.setBounds(beginX+=210, beginY, 60, high);
		frame.add(excelLabel);
		frame.add(excelField);
		frame.add(excelButton);
		
		beginX = 30;beginY+=30;
		JLabel globalLabel = new JLabel("global url:");
		globalField = new JTextField(50);
		globalButton = new Button("choose");
		globalLabel.setBounds(beginX, beginY, 80, high);
		globalField.setBounds(beginX+=85, beginY, 200, high);
		globalButton.setBounds(beginX+=210, beginY, 60, high);
		frame.add(globalLabel);
		frame.add(globalField);
		frame.add(globalButton);
		
		beginX = 30;beginY+=30;
		JLabel outLabel = new JLabel("out url:");
		outField = new JTextField(50);
		outButton = new Button("choose");
		outLabel.setBounds(beginX, beginY, 80, high);
		outField.setBounds(beginX+=85, beginY, 200, high);
		outButton.setBounds(beginX+=210, beginY, 60, high);
		frame.add(outLabel);
		frame.add(outField);
		frame.add(outButton);
		
		beginX = 180;beginY+=30;
		okButton = new Button("ok");
		okButton.setBounds(beginX, beginY, 50, high);
		frame.add(okButton);
		
		frame.setVisible(true);
		event();
	}
	
	private void event() {
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				 frame.setVisible(false);
			}
		});
		
		imageButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				imageField.setText(URL);
			}
		});
		
		outButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				outField.setText(URL);
			}
		});
		
		annotationButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				annotationField.setText(URL);
			}
		});
		
		excelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				excelField.setText(URL);
			}
		});
		
		globalButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				globalField.setText(URL);
			}
		});
		
		
		okButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(!JUDGE) {
					new Thread(()->{
						JUDGE = true;
						try {
							String imageUrl = imageField.getText(),globalUrl = globalField.getText().trim(), excelUrl = excelField.getText().trim(), outUrl = outField.getText();
							String levelUrl = globalUrl+"/cache/level.xls", nameUrl = globalUrl+"/cache/rgb_name.xls";
							String annotationUrl = annotationField.getText().trim();
							ImagePlus image = new ImagePlus(imageUrl);
							int lenX = image.getWidth(), lenY = image.getHeight(),
									lenZ = image.getStackSize();
							ImagePlus annotation = new ImagePlus(annotationUrl);
							
							Map<Integer, String> valToNameMap = new HashMap<>();
							if(!excelUrl.equals("Default all region")) {
								try {
									String[][] excelStrArr = ExcelTools.getExcelTools().getData(new File(excelUrl), 0);
									for(int i=0;i<excelStrArr.length;i++) valToNameMap.put(i+1, excelStrArr[i][0]);
									int bitsDepth = excelStrArr.length>=255?16:8;
									annotation = FuseDemoNewRight.fuseDemo(levelUrl, nameUrl, excelUrl, annotation, bitsDepth);
								} catch (Exception e2) {
									IJ.log(e2.getMessage());
								}
							}else {
								try {
									String[][] excelStrArr = ExcelTools.getExcelTools().getData(new File(nameUrl), 0);
									for(int i=0;i<excelStrArr.length;i++) valToNameMap.put(Integer.valueOf(excelStrArr[i][0]), excelStrArr[i][1]);
								} catch (Exception e2) {
									IJ.log(e2.getMessage());
								}
							}
							int pdX = 0,pdY = 0,upZ = 0;
							int aX = annotation.getWidth(), aY = annotation.getHeight(), aZ = annotation.getStackSize();
							String brainNum = "",altasNum="";
							try {
								DocumentBuilderFactory factorys = DocumentBuilderFactory.newInstance();
								DocumentBuilder db=factorys.newDocumentBuilder();
								Document document = db.parse(new File(globalUrl+"/generate/dataset.xml"));
								document.getDocumentElement().normalize();
								Element brainMapEle = (Element)document.getElementsByTagName("brainMap").item(0);
								Element downSampleEle = (Element)brainMapEle.getElementsByTagName("downSample").item(0);
								Element orgImageEle = (Element)downSampleEle.getElementsByTagName("orgImage").item(0);
								Element tempImageEle = (Element)downSampleEle.getElementsByTagName("tempImage").item(0);
								Element padZeroEle = (Element)orgImageEle.getElementsByTagName("padZero").item(0);
								Element orgImageDownEle = (Element)orgImageEle.getElementsByTagName("orgImageDown").item(0);
								Element tempImageDownEle = (Element)tempImageEle.getElementsByTagName("tempImageDown").item(0);
								Element orgImageSizeEle = (Element)orgImageEle.getElementsByTagName("orgImageSize").item(0);
								brainNum = orgImageDownEle.getTextContent();
								altasNum = tempImageDownEle.getTextContent();
								String[] axs = padZeroEle.getTextContent().split(",");
								String[] temp = orgImageSizeEle.getTextContent().split(",");
								upZ = Integer.valueOf(temp[temp.length-1]);
								pdX = Integer.parseInt(axs[0]);
								pdY = Integer.parseInt(axs[1]);
							} catch (Exception e2) {
								JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
							}
							
							ImagePlus cutAnnotation = NewImage.createByteImage("cut", aX-pdX, aY-pdY, aZ, NewImage.FILL_BLACK);
							ImageStack cutAnnotationStack = cutAnnotation.getImageStack(),annotationStack = annotation.getImageStack();
							for(int z=0;z<aZ;z++){
								for(int y=0;y<aY-pdY&&y<aY;y++){
									for(int x=0;x<aX-pdX&&x<aX;x++){
										cutAnnotationStack.setVoxel(x, y, z, annotationStack.getVoxel(x, y, z));
									}
								}
							}
							cutAnnotation.setStack(cutAnnotationStack);
							
							if(!new File(globalUrl+"/cache/rgb_name.xls").exists()) {
								FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("rgb_name.xls"), globalUrl+"/cache/rgb_name.xls");
							}
							if(!new File(globalUrl+"/cache/level.xls").exists()) {
								FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("level.xls"), globalUrl+"/cache/level.xls");
							}
							
							ImagePlus upAnnotationTemp = UpSampleTools.zRestoreShort(brainNum, altasNum, cutAnnotation, upZ);
							
							ImagePlus outAnnotation = NewImage.createImage("outAnnotation", lenX, lenY, lenZ,annotation.getBitDepth(), NewImage.FILL_BLACK);
							ImageStack upAnnotationTempStack = upAnnotationTemp.getImageStack(),
									outAnnotationStack = outAnnotation.getImageStack();
							
							double scale = (double)upZ/(double)lenZ;
							for(int z=1;z<=lenZ;z++) {
								outAnnotationStack.setProcessor(upAnnotationTempStack.getProcessor((int)Math.ceil(scale*z-scale/2.0)).resize(lenX, lenY, false)
										, z);
							}
							outAnnotation.setStack(outAnnotationStack);
							new File(GlobalValue.URL+"/visual").mkdirs();
							GetBox3DExcel.getBox3DExcel(outAnnotation, outUrl+"/wholeBrain.xls");
							ImageStack imageStack = image.getImageStack();
							try {
								String[][] boxStrArr = ExcelTools.getExcelTools().getData(new File(outUrl+"/wholeBrain.xls"), 0);
								int len = boxStrArr.length;
								int iter = len/MAXTHREAD+(len%MAXTHREAD>0?1:0);
								for(int i=0;i<iter;i++) {
									int num = (len-i*MAXTHREAD)>MAXTHREAD?MAXTHREAD:(len-i*MAXTHREAD);
									CountDownLatch count = new CountDownLatch(num);
									for(int l=i*MAXTHREAD;l<i*MAXTHREAD+num;l++) {
										final int ll = l;
										new Thread(()->{
											int val = Integer.valueOf(boxStrArr[ll][0]),beginX = Integer.valueOf(boxStrArr[ll][1]),beginY = Integer.valueOf(boxStrArr[ll][2]),
													beginZ = Integer.valueOf(boxStrArr[ll][3]),endX = Integer.valueOf(boxStrArr[ll][4]),endY = Integer.valueOf(boxStrArr[ll][5]),
													endZ = Integer.valueOf(boxStrArr[ll][6]);
											ImagePlus out = NewImage.createImage(valToNameMap.get(val), endX-beginX, endY-beginY, endZ-beginZ, image.getBitDepth(), NewImage.FILL_BLACK);
											ImageStack outStack = out.getImageStack();
											for(int z=beginZ;z<endZ;z++) {
												for(int y=beginY;y<endY;y++) {
													for(int x=beginX;x<endX;x++) {
														if((int)outAnnotationStack.getVoxel(x, y, z)==val)outStack.setVoxel(x-beginX, y-beginY, z-beginZ, imageStack.getVoxel(x, y, z));
													}
												}
											}
											out.setStack(outStack);
											new FileSaver(out).saveAsTiff(outUrl+"/"+valToNameMap.get(val)+".tif");
											count.countDown();
										}).start();
									}
									try {
										count.await();
									} catch (Exception e2) {
										IJ.log(e2.getMessage());
									}
								}
								
							} catch (Exception e2) {
								IJ.log(e2.getMessage());
							}
						} catch (Exception e2) {
							IJ.log(e2.getMessage());
						}finally {
							JUDGE = false;
						}
						JUDGE = false;
					}).start();
				}
	            
			}
		});
	}
	
	@Override
	public void run(String arg) {
		location();
	}
	
	public static void main(String[] args) {
		CaculRegionCounting c = new CaculRegionCounting();
		c.run(null);
		c.imageField.setText("D:\\han\\YH298_161209\\YH298_Ch01_Stack[1-1075]_DS10_8bit.tif");
		c.annotationField.setText("I:\\birds\\registration\\coarse\\out_result\\result.tif");
		c.excelField.setText("I:\\han\\dl\\region_19_judge.xls");
		c.globalField.setText("I:\\birds");
		c.outField.setText("I:\\han\\delete\\out");
	}
}
