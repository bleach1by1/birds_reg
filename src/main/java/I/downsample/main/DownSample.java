package I.downsample.main;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import I.plugin.GlobalValue;
import I.plugin.LogIn;
import I.plugin.ThreadPool;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

/**
 * the downsample GUI
 * @author zengweilin
 *
 */
public class DownSample {

	/**needn't to construct this method*/
	private DownSample() {}
	
	public static void main(String[] args) throws IOException{
		DownSample d = new DownSample();
		Frame f = new Frame();
		f.setSize(500, 200);
		f.add(d.location());
		d.ourBrainField1.setText("217");
		d.ourBrainField2.setText("381");
		d.ourBrainField3.setText("502");
		d.ourBrainField4.setText("582");
		d.ourBrainField5.setText("721");
		d.ourBrainField6.setText("941");
		d.ourBrainField7.setText("1084");
		f.setVisible(true);
	}
	
	/**output result of single mode*/
	private static DownSample downsample = null;
	
	/**the input image's name*/
	private JTextField ourTextField;
	
	//private JTextField 
	/**resolution of input data*/
	private JTextField resolutionXField,resolutionYField,resolutionZField;
	/**Feature plane selection of input data*/
	private JTextField ourBrainField1,ourBrainField2,ourBrainField3,ourBrainField4,ourBrainField5,ourBrainField6,ourBrainField7;
	/**Feature plane selection of template data*/
	private JTextField atlasField1,atlasField2,atlasField3,atlasField4,atlasField5,atlasField6,atlasField7;
	/**choose our brain*/
	private Button ourBrainButton;
	/**finish the down sample*/
	private Button okButton;
	
	private ImagePlus ourBrainImage;
	private ImagePlus Atlas132labelImage;
	private ImagePlus tempInvertOrgImage;
	private ImagePlus tempOrgImage;
	private ImagePlus annotationImage;
	
	private String ourBrainStr = "",atlasStr = ""; 
	
	public final static DownSample getDownsample() {
		if(downsample==null) {
			synchronized (DownSample.class) {
				if(downsample==null) {
					downsample = new DownSample();
				}
			}
		}
		return downsample;
	}
	
	public void event() {
		
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				LogIn.show("loss begin...");
				
				Atlas132labelImage.close();
				double resolutionXDouble = Double.valueOf(resolutionXField.getText()),
					resolutionYDouble = Double.valueOf(resolutionYField.getText()),
					resolutionZDouble = Double.valueOf(resolutionZField.getText());
				int resizeX = (int)(resolutionXDouble*(double)ourBrainImage.getWidth()/GlobalValue.REGVOXEL),
					resizeY = (int)(resolutionYDouble*(double)ourBrainImage.getHeight()/GlobalValue.REGVOXEL),
					resizeZ = (int)(resolutionZDouble*(double)ourBrainImage.getStackSize()/GlobalValue.REGVOXEL);
		/*		padX = lenX-resizeX;padY = lenY-resizeY;*/
				ourBrainStr = "";atlasStr = ""; 
				ourBrainStr+=(ourBrainField1.getText().length()==0||atlasField1.getText().length()==0)?"":ourBrainField1.getText()+",";
				ourBrainStr+=(ourBrainField2.getText().length()==0||atlasField2.getText().length()==0)?"":ourBrainField2.getText()+",";
				ourBrainStr+=(ourBrainField3.getText().length()==0||atlasField3.getText().length()==0)?"":ourBrainField3.getText()+",";
				ourBrainStr+=(ourBrainField4.getText().length()==0||atlasField4.getText().length()==0)?"":ourBrainField4.getText()+",";
				ourBrainStr+=(ourBrainField5.getText().length()==0||atlasField5.getText().length()==0)?"":ourBrainField5.getText()+",";
				ourBrainStr+=(ourBrainField6.getText().length()==0||atlasField6.getText().length()==0)?"":ourBrainField6.getText()+",";
				ourBrainStr+=(ourBrainField7.getText().length()==0||atlasField7.getText().length()==0)?"":ourBrainField7.getText()+",";
				
				atlasStr+=(ourBrainField1.getText().length()==0||atlasField1.getText().length()==0)?"":atlasField1.getText()+",";
				atlasStr+=(ourBrainField2.getText().length()==0||atlasField2.getText().length()==0)?"":atlasField2.getText()+",";
				atlasStr+=(ourBrainField3.getText().length()==0||atlasField3.getText().length()==0)?"":atlasField3.getText()+",";
				atlasStr+=(ourBrainField4.getText().length()==0||atlasField4.getText().length()==0)?"":atlasField4.getText()+",";
				atlasStr+=(ourBrainField5.getText().length()==0||atlasField5.getText().length()==0)?"":atlasField5.getText()+",";
				atlasStr+=(ourBrainField6.getText().length()==0||atlasField6.getText().length()==0)?"":atlasField6.getText()+",";
				atlasStr+=(ourBrainField7.getText().length()==0||atlasField7.getText().length()==0)?"":atlasField7.getText()+",";
				
				if(ourBrainStr.length()>0) {
					ourBrainStr = ourBrainStr.substring(0, ourBrainStr.length()-1);
					atlasStr = atlasStr.substring(0, atlasStr.length()-1);
				}
				
				LogIn.show("atlas slices choose is: "+ourBrainStr);
				LogIn.show("our slices choose is:"+atlasStr);
				LogIn.show("begin down sample......");
				
				final String ourBrainStrFin = ourBrainStr;
				final String atlasStrFin = atlasStr;
				CountDownLatch count = new CountDownLatch(1);
				
				if(tempInvertOrgImage!=null) tempInvertOrgImage.close();
				if(annotationImage!=null) annotationImage.close();
				if(tempOrgImage!=null) tempOrgImage.close();
				CountDownLatch count1 = new CountDownLatch(3);
				new Thread(()->{
					tempInvertOrgImage = new ImagePlus(GlobalValue.URL+"/cache/orgInvertImage.tif");
					count1.countDown();
				}).start();
				new Thread(()->{
					annotationImage = new ImagePlus(GlobalValue.URL+"/cache/annotationOrgImage.tif");
					count1.countDown();
				}).start();
				new Thread(()->{
					tempOrgImage = new ImagePlus(GlobalValue.URL+"/cache/tempOrgImage.tif");
					count1.countDown();
				}).start();
				try {
					count1.await();
				} catch (Exception e2) {
					GlobalValue.INTERACTION = false;
				}
				
				ImageStack annotationImageStack = annotationImage.getImageStack();
				ImageStack tempInvertOrgImageStack = tempInvertOrgImage.getImageStack();
				ImageStack tempOrgImageStack = tempOrgImage.getImageStack();
				
				final int resizeZZ = resizeZ, resizeXX = resizeX, resizeYY = resizeY;
				new Thread(()->{
					String[] ourBrainListStr = ourBrainStrFin.trim().split(",");
					String[] atlasListStr = atlasStrFin.trim().split(",");
					int[] atlasInt = DownSampleToAtlasTools.StringToInt(atlasListStr);
					int[] ourBrainInt = DownSampleToAtlasTools.StringToInt(ourBrainListStr);
					
					if(ourBrainInt.length<2) {
						ourBrainInt = new int[] {1,ourBrainImage.getStackSize()};
						atlasInt = new int[] {1,GlobalValue.ALLENLENZ};
					}
					
					int cutZ = (int)((double)(ourBrainInt[ourBrainInt.length-1]-ourBrainInt[0]+1)*resolutionZDouble/GlobalValue.REGVOXEL);
					int[] paragraphs = new int[ourBrainInt.length-1];
					int temp = 0;
					for(int i=0;i<paragraphs.length;i++) {
						paragraphs[i] = (int)Math.round((double)(atlasInt[i+1]-atlasInt[0])/(double)(atlasInt[atlasInt.length-1]-atlasInt[0])*(double)cutZ)-temp;
						temp += paragraphs[i];
					}
					
					
					if(atlasInt.length>3) {
						for(int z=0;z<atlasInt[0]*5-2;z++) {
							for(int y=0;y<GlobalValue.ALLENLENY;y++) {
								for(int x=0;x<GlobalValue.ALLENLENX;x++) {
									annotationImageStack.setVoxel(x, y, z, 0);
									tempInvertOrgImageStack.setVoxel(x, y, z, 0);
									tempOrgImageStack.setVoxel(x, y, z, 0);
								}
							}
						}
						
						for(int z=atlasInt[atlasInt.length-1]*5-2;z<GlobalValue.ALLENLENZ;z++) {
							for(int y=0;y<GlobalValue.ALLENLENY;y++) {
								for(int x=0;x<GlobalValue.ALLENLENX;x++) {
									annotationImageStack.setVoxel(x, y, z, 0);
									tempInvertOrgImageStack.setVoxel(x, y, z, 0);
									tempOrgImageStack.setVoxel(x, y, z, 0);
								}
							}
						}
					}
					
					GlobalValue.downSampleImage = NewImage.createByteImage("downSampleImage", resizeXX, resizeYY, resizeZZ, NewImage.FILL_BLACK);
					ImageStack outImageTempStack = GlobalValue.downSampleImage.getImageStack();
					ImageStack ourBrainStack = ourBrainImage.getImageStack();
					int index = 0,begin = (int)Math.ceil((double)ourBrainInt[0]/(double)ourBrainImage.getStackSize()*(double)resizeZZ);
					for(int i=0;i<atlasInt.length-1;i++) {
						LogIn.schedule(atlasInt.length+"", i+1+"", "dowmsample:/t");
						double ratio = (double)(ourBrainInt[i+1]-ourBrainInt[i])/(double)paragraphs[i];
						int interval = paragraphs[i],maxThread = GlobalValue.MAXTHREAD,
								batch = interval/maxThread+(interval%maxThread>0?1:0);
						for(int b=0;b<batch;b++) {
							int num = (interval-b*maxThread)>maxThread?maxThread:(interval-b*maxThread);
							CountDownLatch count2 = new CountDownLatch(num);
							for(int loc=b*maxThread;loc<num+b*maxThread;loc++) {
								final int ourBrainInti=ourBrainInt[i],locc = loc,indexx = index;
								Thread t = new Thread(()->{
									ImageProcessor tempProcessor = ourBrainStack.getProcessor(ourBrainInti+(int)Math.floor(ratio*((double)locc+0.5))).resize(resizeX, resizeY).convertToByteProcessor();
									if(indexx+locc+begin<=resizeZZ)
										outImageTempStack.setProcessor(tempProcessor, indexx+locc+begin);
									/*
									else
										IJ.error("Downsampling out of bounds");
									*/
									count2.countDown();
								});
								t.start();
							}
							try {
								count2.await();
							} catch (Exception e2) {}
						}
						index += interval;
					}
					
					count.countDown();
				}).start();
				try {
					count.await();
				} catch (Exception e2) {}
				
				annotationImage.setStack(annotationImageStack);
				tempInvertOrgImage.setStack(tempInvertOrgImageStack);
				tempOrgImage.setStack(tempOrgImageStack);
				
				GlobalValue.downSampleImage.show();
				tempOrgImage.show();
				
				FileSaver outImageSaver = new FileSaver(GlobalValue.downSampleImage);
				FileSaver annotationImageSaver = new FileSaver(annotationImage);
				FileSaver tempInvertImageSaver = new FileSaver(tempInvertOrgImage); 
				FileSaver tempImageSaver = new FileSaver(tempOrgImage); 
				
				outImageSaver.saveAsTiff(GlobalValue.URL+"/registration/coarse/downSampleImage.tif");
				annotationImageSaver.saveAsTiff(GlobalValue.URL+"/registration/coarse/annotationImage.tif");
				tempInvertImageSaver.saveAsTiff(GlobalValue.URL+"/registration/coarse/tempInvertImage.tif");
				tempImageSaver.saveAsTiff(GlobalValue.URL+"/registration/coarse/tempImage.tif");
				
				DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		        try {
		            DocumentBuilder db=factory.newDocumentBuilder();
		            Document document;
		            Element brainMapEle;
		            if(new File(GlobalValue.URL+"/generate/dataset.xml").exists()){
		            	document = db.parse(new File(GlobalValue.URL+"/generate/dataset.xml"));
						document.getDocumentElement().normalize();
						brainMapEle = (Element)document.getElementsByTagName("brainMap").item(0);
		            }else{
		            	document=db.newDocument();
		            	document.setXmlStandalone(true);
		            	brainMapEle = document.createElement("brainMap");
		            }
		            Element downSampleEle=document.createElement("downSample");
		           // Element padEle = document.createElement("padZero");
		            Element orgImageEle=document.createElement("orgImage");
		            Element tempImageEle=document.createElement("tempImage");
		            Element orgImageDownEle=document.createElement("orgImageDown");
		            Element tempImageDownEle=document.createElement("tempImageDown");
		            Element orgImageSizeEle=document.createElement("orgImageSize");
		            Element orgImageResulationEle = document.createElement("resolution");
		            
		          //  padEle.setTextContent(padX+","+padY);
		            orgImageResulationEle.setAttribute("unit", "um");
		            orgImageSizeEle.setAttribute("name", ourBrainImage.getTitle());
		            orgImageSizeEle.setTextContent(ourBrainImage.getWidth()+","+ourBrainImage.getHeight()+","+
		            								ourBrainImage.getStackSize());
		            orgImageResulationEle.setTextContent(resolutionXField.getText()+","+resolutionYField.getText()+","+
		            								resolutionZField.getText());
		            orgImageDownEle.setTextContent(ourBrainStr);
		            tempImageDownEle.setTextContent(atlasStr);
		            orgImageEle.appendChild(orgImageSizeEle);
		            orgImageEle.appendChild(orgImageDownEle);
		            orgImageEle.appendChild(orgImageResulationEle);
		            
		            tempImageEle.appendChild(tempImageDownEle);

		            downSampleEle.appendChild(orgImageEle);
		            downSampleEle.appendChild(tempImageEle);
		            
		            brainMapEle.appendChild(downSampleEle);
		            
		            document.appendChild(brainMapEle);
		            
		            TransformerFactory transformerFactory=TransformerFactory.newInstance();
		            try {
		                Transformer tf=transformerFactory.newTransformer();
		                
		                tf.setOutputProperty(OutputKeys.INDENT, "yes");
		                new File(GlobalValue.URL+"/generate").mkdirs();
		                tf.transform(new DOMSource(document),
		                        new StreamResult(new File(GlobalValue.URL+"/generate/dataset.xml")));
		                
		                
		            } catch (TransformerConfigurationException e1) { }
		        } catch (Exception e1) {}
				ImagePlus tempDownSampleImage = new ImagePlus("downSampleImage", GlobalValue.downSampleImage.getImageStack());
				GlobalValue.downSampleImage = tempDownSampleImage;
		        LogIn.show("finish dowm sample!");
				JOptionPane.showMessageDialog(null, "finish the downSample", "finish",JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		ourBrainButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(GlobalValue.URL==null) {
					JOptionPane.showMessageDialog(null, "have not url", "error",JOptionPane.PLAIN_MESSAGE);
					LogIn.show("you need choose a global url");
					return;
				}
				
				try {
					ourBrainImage = IJ.getImage(); 
				} catch (Exception e2) {
					LogIn.show("you need input a brain-image in imageJ\n");
				}
				LogIn.show("success load raw image");
				ThreadPool.getThreadPool().execute(()->{
					ourTextField.setText(ourBrainImage.getTitle());
					if(Atlas132labelImage!=null) Atlas132labelImage.close();
					String atlasUrl = GlobalValue.URL+"/cache/Atlas132label.tif";
					Atlas132labelImage = new ImagePlus(atlasUrl);
					Atlas132labelImage.show();
					atlasField2.setText("43");atlasField3.setText("56");
					atlasField4.setText("62");atlasField5.setText("78");
					atlasField6.setText("105");
				});
				
				LogIn.show("begin choose characteristic planes");
			}
		});
		
	}
	
	public JPanel location() {
		JPanel outPanel = new JPanel(){
			private static final long serialVersionUID = 1L;
			@Override
        	   public Dimension getPreferredSize() {
        	     return new Dimension(500, 200);
        	   }
        }; 
		outPanel.setLayout(null);
		
		int beginX = 30,beginY = 10,high = 25;
		JLabel ourLabel = new JLabel("our:");
		ourTextField = new JTextField(20);
		ourBrainButton = new Button("choose");
		ourLabel.setBounds(beginX,beginY,60,high);
		ourTextField.setBounds(beginX+=60, beginY, 230, high);
		ourBrainButton.setBounds(beginX+=230, beginY, 50, high);
		outPanel.add(ourLabel);
		outPanel.add(ourTextField);
		outPanel.add(ourBrainButton);
		
		beginX = 30;beginY+=35;
		JLabel resolutionXLabel = new JLabel("resolutionX:");
		resolutionXField = new JTextField(5);
		JLabel micron0 = new JLabel("um");
		JLabel resolutionYLabel = new JLabel("resolutionY:");
		resolutionYField = new JTextField(5);
		JLabel micron1 = new JLabel("um");
		JLabel resolutionZLabel = new JLabel("resolutionZ:");
		resolutionZField = new JTextField(5);
		JLabel micron2 = new JLabel("um");
		resolutionXLabel.setBounds(beginX, beginY, 70, high);
		resolutionXField.setBounds(beginX+=70, beginY, 25, high);
		micron0.setBounds(beginX+=25, beginY, 30, high);
		resolutionYLabel.setBounds(beginX+=40, beginY, 70, high);
		resolutionYField.setBounds(beginX+=70, beginY, 25, high);
		micron1.setBounds(beginX+=25, beginY, 30, high);
		resolutionZLabel.setBounds(beginX+=40, beginY, 70, high);
		resolutionZField.setBounds(beginX+=70, beginY, 25, high);
		micron2.setBounds(beginX+=25, beginY, 30, high);
		outPanel.add(resolutionXLabel);
		outPanel.add(resolutionXField);
		outPanel.add(micron0);
		outPanel.add(resolutionYLabel);
		outPanel.add(resolutionYField);
		outPanel.add(micron1);
		outPanel.add(resolutionZLabel);
		outPanel.add(resolutionZField);
		outPanel.add(micron2);
		
		beginX = 30;beginY+=35;
		JLabel ourBrainLabel = new JLabel("ourBrain:");
		ourBrainField1 = new JTextField(2);
		ourBrainField2 = new JTextField(2);
		ourBrainField3 = new JTextField(2);
		ourBrainField4 = new JTextField(2);
		ourBrainField5 = new JTextField(2);
		ourBrainField6 = new JTextField(2);
		ourBrainField7 = new JTextField(2);
		JLabel ourBrain1Label = new JLabel("-");
		JLabel ourBrain2Label = new JLabel("-");
		JLabel ourBrain3Label = new JLabel("-");
		JLabel ourBrain4Label = new JLabel("-");
		JLabel ourBrain5Label = new JLabel("-");
		JLabel ourBrain6Label = new JLabel("-");
		
		ourBrainLabel.setBounds(beginX,beginY,60,high);
		ourBrainField1.setBounds(beginX+=60, beginY, 30, high);
		ourBrain1Label.setBounds(beginX+=35, beginY, 10, high);
		ourBrainField2.setBounds(beginX+=10, beginY, 30, high);
		ourBrain2Label.setBounds(beginX+=35, beginY, 10, high);
		ourBrainField3.setBounds(beginX+=10, beginY, 30, high);
		ourBrain3Label.setBounds(beginX+=35, beginY, 10, high);
		ourBrainField4.setBounds(beginX+=10, beginY, 30, high);
		ourBrain4Label.setBounds(beginX+=35, beginY, 10, high);
		ourBrainField5.setBounds(beginX+=10, beginY, 30, high);
		ourBrain5Label.setBounds(beginX+=35, beginY, 10, high);
		ourBrainField6.setBounds(beginX+=10, beginY, 30, high);
		ourBrain6Label.setBounds(beginX+=35, beginY, 10, high);
		ourBrainField7.setBounds(beginX+=10, beginY, 30, high);
		outPanel.add(ourBrainLabel);
		outPanel.add(ourBrainField1);
		outPanel.add(ourBrain1Label);
		outPanel.add(ourBrainField2);
		outPanel.add(ourBrain2Label);
		outPanel.add(ourBrainField3);
		outPanel.add(ourBrain3Label);
		outPanel.add(ourBrainField4);
		outPanel.add(ourBrain4Label);
		outPanel.add(ourBrainField5);
		outPanel.add(ourBrain5Label);
		outPanel.add(ourBrainField6);
		outPanel.add(ourBrain6Label);
		outPanel.add(ourBrainField7);
		
		beginX = 30;beginY+=35;
		JLabel atlasLabel = new JLabel("atlas:");
		atlasField1 = new JTextField(2);
		atlasField2 = new JTextField(2);
		atlasField3 = new JTextField(2);
		atlasField4 = new JTextField(2);
		atlasField5 = new JTextField(2);
		atlasField6 = new JTextField(2);
		atlasField7 = new JTextField(2);
		JLabel ourAtlas1Label = new JLabel("-");
		JLabel ourAtlas2Label = new JLabel("-");
		JLabel ourAtlas3Label = new JLabel("-");
		JLabel ourAtlas4Label = new JLabel("-");
		JLabel ourAtlas5Label = new JLabel("-");
		JLabel ourAtlas6Label = new JLabel("-");
		atlasLabel.setBounds(beginX,beginY,60,high);
		atlasField1.setBounds(beginX+=60, beginY, 30, high);
		ourAtlas1Label.setBounds(beginX+=35, beginY, 10, high);
		atlasField2.setBounds(beginX+=10, beginY, 30, high);
		ourAtlas2Label.setBounds(beginX+=35, beginY, 10, high);
		atlasField3.setBounds(beginX+=10, beginY, 30, high);
		ourAtlas3Label.setBounds(beginX+=35, beginY, 10, high);
		atlasField4.setBounds(beginX+=10, beginY, 30, high);
		ourAtlas4Label.setBounds(beginX+=35, beginY, 10, high);
		atlasField5.setBounds(beginX+=10, beginY, 30, high);
		ourAtlas5Label.setBounds(beginX+=35, beginY, 10, high);
		atlasField6.setBounds(beginX+=10, beginY, 30, high);
		ourAtlas6Label.setBounds(beginX+=35, beginY, 10, high);
		atlasField7.setBounds(beginX+=10, beginY, 30, high);
		outPanel.add(atlasLabel);
		outPanel.add(atlasField1);
		outPanel.add(atlasField2);
		outPanel.add(atlasField3);
		outPanel.add(atlasField4);
		outPanel.add(atlasField5);
		outPanel.add(atlasField6);
		outPanel.add(atlasField7);
		outPanel.add(ourAtlas1Label);
		outPanel.add(ourAtlas2Label);
		outPanel.add(ourAtlas3Label);
		outPanel.add(ourAtlas4Label);
		outPanel.add(ourAtlas5Label);
		outPanel.add(ourAtlas6Label);
		
		beginX = 180;beginY+=35;
		okButton = new Button("ok");
		okButton.setBounds(beginX+=30, beginY, 30, high);
		outPanel.add(okButton);
		event();
		return outPanel;
	}

}
