package I.plugin;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import I.coarse.main.CoarseRegistration;
import I.deeplearning.Segnet;
import I.downsample.main.DownSample;
import I.precise.main.PreciseRegistration;
import I.precise.tools.LineTools;
import I.utils.DownloadTools;
import I.utils.FileUnZipRar;
import I.visualization.main.Visualization;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;
import ij.process.ImageProcessor;

/**
 * the plugIn main GUI 
 * @author zengweilin
 *
 */

public class BirdsPlugin implements PlugIn{
	
	
	public static JTextArea SOUTHTEXT;

	/**the body of plugin*/
	public JFrame frame;
	
	/**the global url to be choose*/
	public JTextField urlText;
	public Button urlButton;
	
	/**location in south body*/
	public JScrollPane southJsp;
	
	/**location in center body*/
	public JTabbedPane centerPanel;
	
	public void location() {
		JPanel northPanel = new JPanel();
		Image loggImage = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("brain.png"));
		loggImage = loggImage.getScaledInstance(70, 50, Image.SCALE_DEFAULT);
		ImageIcon loggImageIcon = new ImageIcon(loggImage);
		JLabel loggImageLabel = new JLabel(loggImageIcon);
		JLabel urlLabel = new JLabel("url:");
		urlText = new JTextField(20);
		urlButton = new Button("choose");
		northPanel.add(loggImageLabel);
		northPanel.add(urlLabel);
		northPanel.add(urlText);
		northPanel.add(urlButton);
		
		centerPanel = new JTabbedPane();
		
		Image tabImage = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/bluePoint.png"));
		tabImage = tabImage.getScaledInstance(8, 8, Image.SCALE_DEFAULT);
		
		centerPanel.addTab("downsample",new ImageIcon(tabImage),DownSample.getDownsample().location());
		centerPanel.addTab("coarse", new ImageIcon(tabImage), CoarseRegistration.getCoarseregistarion().location());
		centerPanel.addTab("precise", new ImageIcon(tabImage), PreciseRegistration.getPreciseregistration().location());
		centerPanel.addTab("visual", new ImageIcon(tabImage), Visualization.getVisualization().location());
		centerPanel.addTab("setSeg", new ImageIcon(tabImage), Segnet.getSegnet().location());
		
		SOUTHTEXT = new JTextArea("welcome use BIRDS\n");
		SOUTHTEXT.setLineWrap(true); 
		SOUTHTEXT.setWrapStyleWord(true);
		SOUTHTEXT.setEditable(false);
		
		southJsp = new JScrollPane(SOUTHTEXT){
			private static final long serialVersionUID = 1L;
			@Override
    	   public Dimension getPreferredSize() {
    	     return new Dimension(400, 280);
    	   }
        };
        
        frame = new JFrame("BIRDS");
        frame.add(southJsp,BorderLayout.SOUTH);
        frame.add(centerPanel,BorderLayout.CENTER);
        frame.add(northPanel,BorderLayout.NORTH);
        frame.setSize(500, 600);
        frame.setVisible(true);
        frame.setLocation(600, 300);
        
        event();
	}
	
	private void event() {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				PreciseRegistration.annotationImage = null;
				GlobalValue.downSampleImage = null;
				frame.setVisible(false);
			}
		});
		
		urlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				urlText.setText(URL);
				GlobalValue.URL = URL;
				
				PreciseRegistration.annotationImage = null;
				GlobalValue.downSampleImage = null;
				if(PreciseRegistration.getPreciseregistration().fuseImage!=null)
					if(PreciseRegistration.getPreciseregistration().fuseImage.isVisible()) 
						PreciseRegistration.getPreciseregistration().fuseImage.close();
				PreciseRegistration.getPreciseregistration().fuseImage = null;
				
				new File(URL+"/cache").mkdirs();
				new File(URL+"/registration/coarse").mkdirs();
				LogIn.show("set global url: "+GlobalValue.URL);
				LogIn.show("begin load cache......");
				if(!new File(URL+"/cache/annotationOrgImage.tif").exists()||!new File(URL+"/cache/Atlas132label.tif").exists()
						||!new File(URL+"/cache/tempOrgImage.tif").exists()||!new File(URL+"/cache/orgInvertImage.tif").exists()) {
					new File(URL+"/cache");
					try {
						ThreadPool.getThreadPool().execute(()->{
							CountDownLatch count = new CountDownLatch(1);
							ThreadPool.getThreadPool().execute(()->{
								if(!new File(GlobalValue.URL+"/cache/cache-master.zip").exists())
									DownloadTools.downLoadFromUrl(GlobalValue.CACHEURL, "cache-master.zip", URL+"/cache");
								count.countDown();
							});
							try {
								count.await();
								FileUnZipRar.zipRarToFile("cache-master.zip", GlobalValue.URL+"/cache/cache-master.zip", GlobalValue.URL+"/cache");
								FileUnZipRar.zipRarToFile("cache.rar", GlobalValue.URL+"/cache/cache-master/cache-master/cache.rar",
										GlobalValue.URL+"/cache");
								AdditionalFile.saveFile(GlobalValue.URL+"/cache/cache", GlobalValue.URL+"/cache");
								AdditionalFile.deleteDir(new File(GlobalValue.URL+"/cache/cache"));
								AdditionalFile.deleteDir(new File(GlobalValue.URL+"/cache/cache-master"));
								
							} catch (Exception e2) {
								JOptionPane.showMessageDialog(null, "Check the network for download errors", "error",JOptionPane.PLAIN_MESSAGE);
								IJ.log(e2.getMessage());
							}
						});
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Check the network for download errors", "error",JOptionPane.PLAIN_MESSAGE);
						IJ.log(e1.getMessage());
					}
						
				}
				LogIn.show("end load.");
			}
		});
		
		centerPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int type = ((JTabbedPane)arg0.getSource()).getSelectedIndex();
				if(GlobalValue.URL==null&&type!=0) {
					centerPanel.setSelectedIndex(0);
					JOptionPane.showMessageDialog(null, "please choose work environment", "error",JOptionPane.PLAIN_MESSAGE);
					return;
				}
				switch (type) {
				case 1:
					if(GlobalValue.downSampleImage==null) {
						if(new File(GlobalValue.URL+"/registration/coarse/downSampleImage.tif").exists()) {
							GlobalValue.downSampleImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/downSampleImage.tif");
						}else {
							JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
							centerPanel.setSelectedIndex(0);
							return;
						}
					}
					break;
					
				case 2: 
					ThreadPool.getThreadPool().execute(()->{
						//PreciseRegistration.getPreciseregistration().fuseImage = null;
						if(PreciseRegistration.getPreciseregistration().fuseImage==null) {
							if(GlobalValue.downSampleImage==null) {
								if(new File(GlobalValue.URL+"/registration/coarse/downSampleImage.tif").exists()) {
									GlobalValue.downSampleImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/downSampleImage.tif");
								}else {
									JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
									centerPanel.setSelectedIndex(0);
									return;
								}
							}
							if(new File(GlobalValue.URL+"/registration/precise/result.tif").exists()) {
								PreciseRegistration.annotationImage = new ImagePlus(GlobalValue.URL+"/registration/precise/result.tif");
							}else if(new File(GlobalValue.URL+"/registration/coarse/out_result/result.tif").exists()) {
								PreciseRegistration.annotationImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/out_result/result.tif");
							}else {
								JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
								centerPanel.setSelectedIndex(0);
								return;
							}
							
							int lenX = GlobalValue.downSampleImage.getWidth(),
								lenY = GlobalValue.downSampleImage.getHeight(),
								lenZ = GlobalValue.downSampleImage.getStackSize();
							PreciseRegistration.lineImage = NewImage.createByteImage("lineImage", lenX, lenY,
									lenZ, NewImage.FILL_BLACK);
							PreciseRegistration.getPreciseregistration().fuseImage = NewImage.createByteImage("fuseImage", lenX, lenY,
									lenZ, NewImage.FILL_BLACK);
							
							ImageStack annotationImageStack = PreciseRegistration.annotationImage.getImageStack();
							ImageStack lineImageStack = PreciseRegistration.lineImage.getImageStack();
							ImageStack fuseImageStack = PreciseRegistration.getPreciseregistration().fuseImage.getImageStack();
							ImageStack downSampleImageStack = GlobalValue.downSampleImage.getImageStack();
							
							
							int batch = lenZ/GlobalValue.MAXTHREAD+(lenZ%GlobalValue.MAXTHREAD>0?1:0);
							for(int b=0;b<batch;b++) {
								int num = (lenZ-b*GlobalValue.MAXTHREAD)>GlobalValue.MAXTHREAD?GlobalValue.MAXTHREAD:(lenZ-b*GlobalValue.MAXTHREAD);
								CountDownLatch count1 = new CountDownLatch(num);
								for(int z=b*GlobalValue.MAXTHREAD+1;z<=num+b*GlobalValue.MAXTHREAD;z++) {
									final int zz = z;
									new Thread(()->{
										ImageProcessor imageProcessor = annotationImageStack.getProcessor(zz);
										ImageProcessor outProcessor = lineImageStack.getProcessor(zz);
										outProcessor = LineTools.getLineTool().lineMinGet(imageProcessor, outProcessor);
										lineImageStack.setProcessor(outProcessor, zz);
										count1.countDown();
									}).start();
								}
								try {
									count1.await();
								} catch (Exception e) {
									GlobalValue.INTERACTION = false;
									IJ.error(e.getMessage());
								}
							}
							
							RGBStackMerge rgbStackMerge = new RGBStackMerge();
							fuseImageStack = rgbStackMerge.mergeStacks(lenX, lenY, lenZ, downSampleImageStack, lineImageStack,null, true);
							PreciseRegistration.getPreciseregistration().fuseImage.setStack(fuseImageStack);
							PreciseRegistration.getPreciseregistration().addMouseListener();
						}
						if(!PreciseRegistration.getPreciseregistration().fuseImage.isVisible()){
							int lenX = GlobalValue.downSampleImage.getWidth(),
									lenY = GlobalValue.downSampleImage.getHeight(),
									lenZ = GlobalValue.downSampleImage.getStackSize();
							PreciseRegistration.getPreciseregistration().fuseImage = NewImage.createByteImage("fuseImage", lenX, lenY,
									lenZ, NewImage.FILL_BLACK);
							
							if(new File(GlobalValue.URL+"/registration/precise/result.tif").exists()) {
								PreciseRegistration.annotationImage = new ImagePlus(GlobalValue.URL+"/registration/precise/result.tif");
							}else if(new File(GlobalValue.URL+"/registration/coarse/out_result/result.tif").exists()) {
								PreciseRegistration.annotationImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/out_result/result.tif");
							}else {
								JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
								centerPanel.setSelectedIndex(0);
								return;
							}
							
							PreciseRegistration.lineImage = NewImage.createByteImage("lineImage", lenX, lenY,
									lenZ, NewImage.FILL_BLACK);
							
							ImageStack annotationImageStack = PreciseRegistration.annotationImage.getImageStack(), lineImageStack = PreciseRegistration.lineImage.getImageStack();
							int batch = lenZ/GlobalValue.MAXTHREAD+(lenZ%GlobalValue.MAXTHREAD>0?1:0);
							for(int b=0;b<batch;b++) {
								int num = (lenZ-b*GlobalValue.MAXTHREAD)>GlobalValue.MAXTHREAD?GlobalValue.MAXTHREAD:(lenZ-b*GlobalValue.MAXTHREAD);
								CountDownLatch count1 = new CountDownLatch(num);
								for(int z=b*GlobalValue.MAXTHREAD+1;z<=num+b*GlobalValue.MAXTHREAD;z++) {
									final int zz = z;
									new Thread(()->{
										ImageProcessor imageProcessor = annotationImageStack.getProcessor(zz);
										ImageProcessor outProcessor = lineImageStack.getProcessor(zz);
										outProcessor = LineTools.getLineTool().lineMinGet(imageProcessor, outProcessor);
										lineImageStack.setProcessor(outProcessor, zz);
										count1.countDown();
									}).start();
								}
								try {
									count1.await();
								} catch (Exception e) {
									GlobalValue.INTERACTION = false;
									IJ.error(e.getMessage());
								}
							}
							
							ImageStack fuseImageStack = PreciseRegistration.getPreciseregistration().fuseImage.getImageStack();
							ImageStack downSampleImageStack = GlobalValue.downSampleImage.getImageStack();
							
							RGBStackMerge rgbStackMerge = new RGBStackMerge();
							fuseImageStack = rgbStackMerge.mergeStacks(lenX, lenY, lenZ, downSampleImageStack, lineImageStack,null, true);
							PreciseRegistration.getPreciseregistration().fuseImage.setStack(fuseImageStack);
							PreciseRegistration.getPreciseregistration().addMouseListener();
						}
					});
					
				case 3:
					if(PreciseRegistration.annotationImage != null){
						Visualization.annotationImage = PreciseRegistration.annotationImage;
					}else if(new File(GlobalValue.URL+"/registration/precise/result.tif").exists()) {
					Visualization.annotationImage = new ImagePlus(GlobalValue.URL+"/registration/precise/result.tif");
					}else if(new File(GlobalValue.URL+"/registration/coarse/out_result/result.tif").exists()) {
						Visualization.annotationImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/out_result/result.tif");
					}else {
						JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
						centerPanel.setSelectedIndex(0);
						return;
					}
					break;
				default:break;
				}
				
				switch (type) {
					case 0:LogIn.show("use downsample");break;
					case 1:LogIn.show("use coarse");break;
					case 2:LogIn.show("use precise");break;
					case 3:LogIn.show("use visual");break;
					case 4:LogIn.show("use setSeg");break;
					default:break;
				}
			}
		});
		
	}
	
	public static void main(String[] args) {
		new ImageJ();
		new BirdsPlugin().location();
	}

	@Override
	public void run(String arg) {
		new BirdsPlugin().location();
	}

}
