package I.coarse.main;

import java.awt.Button;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import I.coarse.entity.InvertLineEntity;
import I.coarse.entity.LineEntity;
import I.coarse.tools.ChangeParamTools;
import I.coarse.tools.InvertTools;
import I.coarse.tools.PCGenerate;
import I.plugin.GlobalValue;
import I.plugin.LogIn;
import I.plugin.ThreadPool;
import I.utils.ArrowTools;
import I.utils.FileCopyUtils;
import I.utils.RegistarionTools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.process.AutoThresholder;
import ij.process.ColorProcessor;

/**
 * the coarse registaration
 * @author zengweilin
 *
 */
public class CoarseRegistration implements MouseListener{

	private static CoarseRegistration coarseregistarion = null;
	private static double fetrueRatio = 0.125;
	
	private CoarseRegistration() {}
	
	public static CoarseRegistration getCoarseregistarion() {
		if(coarseregistarion==null) {
			synchronized (CoarseRegistration.class) {
				if(coarseregistarion==null) {
					coarseregistarion = new CoarseRegistration();
				}
			}
		}
		return coarseregistarion;
	}
	
	private JCheckBox invertCheck;
	private JCheckBox pcCheck;
	private JCheckBox rigidCheck;
	private JCheckBox affineCheck;
	private JCheckBox bsplineCheck;
	
	private JTextField rowField,PCField,invertField,iterField;
	
	private Button deleteButton;
	private Button invertButton;
	private Button changeModel;
	
	private JLabel xYLabel;
	private JLabel xZLabel;
	private JLabel yZLabel;
	
	private Button okButton;
	
	private Map<Integer, List<LineEntity>> XYMap;
	private Map<Integer, List<LineEntity>> XZMap;
	private Map<Integer, List<LineEntity>> YZMap;
	
	private int invertState;	//the state about invert,1 is XY, 2 is XZ, 3 is YZ, the default is 1
	private int lenX,lenY,lenZ;
	private LineEntity lineEntity;
	
	private MouseListener mouseListener;
	private ImagePlus downSampleImageRGB;
	private ImagePlus invertImage;
	private ImagePlus tempImage;
	private ImagePlus orgPCImage;
	private ImagePlus altasPCImage;
	
	private ImagePlus downSampleImageMask;
	
	private ImageStack downSampleImageRGBStack;
	private ImageStack downSampleImageStack;
	private ImageStack downSampleImageMaskStack;
	
	private void event() {
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(GlobalValue.INTERACTION) {
					JOptionPane.showMessageDialog(null, "other operations did not end", "error",JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				if(GlobalValue.downSampleImage==null) {
					//if(DownSample.downSampleImage!=null) downSampleImage = DownSample.downSampleImage;
					if(new File(GlobalValue.URL+"/registration/coarse/downSampleImage.tif").exists()) {
						GlobalValue.downSampleImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/downSampleImage.tif");
					}else {
						JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
						return;
					}
				}
				
				if(!rigidCheck.isSelected()&&!affineCheck.isSelected()&&!bsplineCheck.isSelected()) {
					JOptionPane.showMessageDialog(null, "Please select the registration model", "error",JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				GlobalValue.INTERACTION = true;
				LogIn.show("begin registration...");
				String orgPCImageUrl = GlobalValue.URL+"/registration/coarse/orgPCImage.tif",
						altasPCImageUrl = GlobalValue.URL+"/registration/coarse/altasPCImage.tif";
				lenX = GlobalValue.downSampleImage.getWidth();lenY = GlobalValue.downSampleImage.getHeight(); lenZ = GlobalValue.downSampleImage.getStackSize();
				downSampleImageStack = GlobalValue.downSampleImage.getImageStack();
				ThreadPool.getThreadPool().execute(()->{
					
					FileSaver invertSaver = new FileSaver(invertImage);
					invertSaver.saveAsTiff(GlobalValue.URL+"/registration/coarse/downSampleInvertImage.tif");
					if(pcCheck.isSelected()&&(!new File(orgPCImageUrl).exists()||!new File(altasPCImageUrl).exists())) {
						LogIn.show("begin pc...");
						
						tempImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/tempImage.tif");
						ImageStack tempImageStack = tempImage.getImageStack();
						
						orgPCImage = NewImage.createByteImage("orgPCImage", lenX, lenY, lenZ, NewImage.FILL_BLACK);
						altasPCImage = NewImage.createByteImage("altasPCImage", GlobalValue.ALLENLENX, GlobalValue.ALLENLENY, GlobalValue.ALLENLENZ, NewImage.FILL_BLACK);
						ImageStack orgPCImageStack = orgPCImage.getImageStack(),altasPCImageStack = altasPCImage.getImageStack();
						
						int PCMaxThread = Runtime.getRuntime().availableProcessors()*2, batch = (GlobalValue.ALLENLENZ/PCMaxThread)+((GlobalValue.ALLENLENZ%PCMaxThread>0)?1:0);
						for(int b=0;b<batch;b++) {
							LogIn.schedule(batch+"", b+"","orgPCConvert:\t");
							int num = ((GlobalValue.ALLENLENZ-b*PCMaxThread)>PCMaxThread)?PCMaxThread:(GlobalValue.ALLENLENZ-b*PCMaxThread);
							CountDownLatch count = new CountDownLatch(num);
							for(int z=b*PCMaxThread;z<b*PCMaxThread+num;z++) {
								final int zz = z+1;
								new Thread(()->{
									altasPCImageStack.setProcessor(PCGenerate.
											image2PC(tempImageStack.getProcessor(zz)).convertToByteProcessor(), zz);
									count.countDown();
								}).start();
							//	ThreadPool.getThreadPool().execute(()->{
							//	});
							}
							try {
								count.await();
							} catch (Exception e2) {
								GlobalValue.INTERACTION = false;
								IJ.log(e2.getMessage());
							}
						}
						
						batch = (lenZ/PCMaxThread)+((lenZ%PCMaxThread>0)?1:0);
						for(int b=0;b<batch;b++) {
							LogIn.schedule(batch+"", b+"","imagePCConvert:\t");
							int num = ((lenZ-b*PCMaxThread)>PCMaxThread)?PCMaxThread:(lenZ-b*PCMaxThread);
							CountDownLatch count = new CountDownLatch(num);
							for(int z=b*PCMaxThread;z<b*PCMaxThread+num;z++) {
								final int zz = z+1;
								new Thread(()->{
									orgPCImageStack.setProcessor(PCGenerate.
											image2PC(downSampleImageStack.getProcessor(zz)).convertToByteProcessor(), zz);
									count.countDown();
								}).start();
							}
							try {
								count.await();
							} catch (Exception e2) {
								GlobalValue.INTERACTION = false;
								IJ.log(e2.getMessage());
							}
						}
						
						
						orgPCImage.setStack(orgPCImageStack);
						altasPCImage.setStack(altasPCImageStack);
						FileSaver orgPCImageSaver = new FileSaver(orgPCImage);
						FileSaver altasPCImageSaver = new FileSaver(altasPCImage);
						
						orgPCImageSaver.saveAsTiff(orgPCImageUrl);
						altasPCImageSaver.saveAsTiff(altasPCImageUrl);
						LogIn.show("end pc...");
					}
					
					boolean invert = invertCheck.isSelected(),pc = pcCheck.isSelected();
					boolean rigid = rigidCheck.isSelected(),affine = affineCheck.isSelected(),bspline = bsplineCheck.isSelected();
					double invertVal = Double.valueOf(invertField.getText().trim())*fetrueRatio,
						pcVal = Double.valueOf(PCField.getText())*fetrueRatio,
						rowVal = Double.valueOf(rowField.getText())*fetrueRatio;
					int registarionModel = (rigid?1:0) + (affine?1:0) + (bspline?1:0)-1;
					int channel = 1+(invert?1:0)+(pc?1:0);
					if(!new File(GlobalValue.URL+"/cache/ANNlib-4.9.dll").exists())
						FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("ANNlib-4.9.dll"), GlobalValue.URL+"/cache/ANNlib-4.9.dll");
					if(!new File(GlobalValue.URL+"/cache/elastix.exe").exists())
						FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("elastix.exe"), GlobalValue.URL+"/cache/elastix.exe");
					if(!new File(GlobalValue.URL+"/cache/transformix.exe").exists())
						FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("transformix.exe"), GlobalValue.URL+"/cache/transformix.exe");
					Map<String, String> changeMap = new HashMap<>();
					changeMap.put("(MaximumNumberOfIterations", iterField.getText().trim());
					changeMap.put("(Metric0Weight", rowVal+"");
					if(invert){
						changeMap.put("(Metric1Weight", invertVal+"");
						if(pc) changeMap.put("(Metric2Weight", pcVal+"");
					}
					else changeMap.put("(Metric1Weight", pcVal+"");
					
					switch (channel) {
					case 1:
						if(rigid){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_rigid.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_rigid.txt"), GlobalValue.URL+"/cache/para-Standard_rigid.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_rigid.txt", changeMap);
						}
						if(affine){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_affine.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_affine.txt"), GlobalValue.URL+"/cache/para-Standard_affine.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_affine.txt", changeMap);
						}
						if(bspline){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_bspline.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_bspline.txt"), GlobalValue.URL+"/cache/para-Standard_bspline.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_bspline.txt", changeMap);
						}
						break;
					case 2:
						if(rigid){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_rigid_mutil2.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_rigid_mutil2.txt"), GlobalValue.URL+"/cache/para-Standard_rigid_mutil2.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_rigid_mutil2.txt", changeMap);
						}
						if(affine){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_affine_mutil2.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_affine_mutil2.txt"), GlobalValue.URL+"/cache/para-Standard_affine_mutil2.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_affine_mutil2.txt", changeMap);
						}
						if(bspline){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_bspline_mutil2.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_bspline_mutil2.txt"), GlobalValue.URL+"/cache/para-Standard_bspline_mutil2.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_bspline_mutil2.txt", changeMap);
						}
						break;
					case 3:
						if(rigid){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_rigid_mutil3.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_rigid_mutil3.txt"), GlobalValue.URL+"/cache/para-Standard_rigid_mutil3.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_rigid_mutil3.txt", changeMap);
						}
						if(affine){
							if(!new File(GlobalValue.URL+"/cache/para-Standard_affine_mutil3.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_affine_mutil3.txt"), GlobalValue.URL+"/cache/para-Standard_affine_mutil3.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_affine_mutil3.txt", changeMap);
						}
						if(bspline){
							if(!new File(GlobalValue.URL+"/para-Standard_bspline_mutil3.txt").exists())
								FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard_bspline_mutil3.txt"), GlobalValue.URL+"/cache/para-Standard_bspline_mutil3.txt");
							ChangeParamTools.changeParam(GlobalValue.URL+"/cache/para-Standard_bspline_mutil3.txt", changeMap);
						}
						break;
					default:break;
					}
					
					new File(GlobalValue.URL+"/registration/coarse/out").mkdirs();
					String cmd = GlobalValue.URL+"/cache/elastix.exe";
					
					cmd+=" -f0 "+GlobalValue.URL+"/registration/coarse/downSampleImage.tif";
					cmd+=" -m0 "+GlobalValue.URL+"/registration/coarse/tempImage.tif";
					
					if(invert){
						cmd+=" -f1 "+GlobalValue.URL+"/registration/coarse/downSampleInvertImage.tif";
						cmd+=" -m1 "+GlobalValue.URL+"/registration/coarse/tempInvertImage.tif";
					}
					
					if(pc&&!invert){
						cmd+=" -f1 "+GlobalValue.URL+"/registration/coarse/orgPCImage.tif";
						cmd+=" -m1 "+GlobalValue.URL+"/registration/coarse/altasPCImage.tif";
					}else if(pc&&invert){
						cmd+=" -f2 "+GlobalValue.URL+"/registration/coarse/orgPCImage.tif";
						cmd+=" -m2 "+GlobalValue.URL+"/registration/coarse/altasPCImage.tif";
					}
					if(rigid){
						switch (channel) {
						case 1:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_rigid.txt";break;
						case 2:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_rigid_mutil2.txt";break;
						case 3:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_rigid_mutil3.txt";break;
						default:break;
						}
					}
					if(affine){
						switch (channel) {
						case 1:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_affine.txt";break;
						case 2:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_affine_mutil2.txt";break;
						case 3:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_affine_mutil3.txt";break;
						default:break;
						}
					}
					if(bspline){
						switch (channel) {
						case 1:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_bspline.txt";break;
						case 2:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_bspline_mutil2.txt";break;
						case 3:cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard_bspline_mutil3.txt";break;
						default:break;
						}
					}
					cmd+=" -out "+GlobalValue.URL+"/registration/coarse/out";
					RegistarionTools.deformation(cmd);
					/*
					try {
						
						paramTools.changeCoarse(GlobalValue.URL+"/registration/coarse/out/TransformParameters."+registarionModel+".txt");
					} catch (Exception e1) {}
					*/
					new File(GlobalValue.URL+"/registration/coarse/out_result").mkdirs();
					LogIn.show("begin deformation...");
					cmd = "";
					cmd = GlobalValue.URL+"/cache/transformix.exe"+
								" -in "+GlobalValue.URL+"/registration/coarse/annotationImage.tif"+
								" -out "+GlobalValue.URL+"/registration/coarse/out_result"+
								" -tp "+GlobalValue.URL+"/registration/coarse/out/TransformParameters."+registarionModel+".txt";
					RegistarionTools.deformation(cmd);
					
					switch (channel) {
					case 1:
						if(rigid) new File(GlobalValue.URL+"/cache/para-Standard_rigid.txt").delete();
						if(affine) new File(GlobalValue.URL+"/cache/para-Standard_affine.txt").delete();
						if(bspline) new File(GlobalValue.URL+"/cache/para-Standard_bspline.txt").delete();
						break;
					case 2:
						if(rigid) new File(GlobalValue.URL+"/cache/para-Standard_rigid_mutil2.txt").delete();
						if(affine) new File(GlobalValue.URL+"/cache/para-Standard_affine_mutil2.txt").delete();
						if(bspline) new File(GlobalValue.URL+"/cache/para-Standard_bspline_mutil2.txt").delete();
						break;
					case 3:
						if(rigid) new File(GlobalValue.URL+"/cache/para-Standard_rigid_mutil3.txt").delete();
						if(affine) new File(GlobalValue.URL+"/cache/para-Standard_affine_mutil3.txt").delete();
						if(bspline) new File(GlobalValue.URL+"/cache/para-Standard_bspline_mutil3.txt").delete();
						break;
					default:break;
					}
					GlobalValue.INTERACTION = false;
					
					LogIn.show("end deformation...");
					LogIn.show("end registration...");
					JOptionPane.showMessageDialog(null, "finish the coarse registration", "finish",JOptionPane.PLAIN_MESSAGE);
				});
				
			}
		});
		
		changeModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (invertState) {
				case 1:invertState++;changeModel.setLabel("XZ"); break;
				case 2:invertState++;changeModel.setLabel("YZ");break;
				case 3:invertState=1;changeModel.setLabel("XY");break;
				}
			}
		});
		
		invertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!invertCheck.isSelected()) {
					JOptionPane.showMessageDialog(null, "you haven't choose invert", "error",JOptionPane.PLAIN_MESSAGE);
					return;
				}
				if(invertImage!=null) invertImage.close();
				int[] size = new int[3];
				size[0] = lenX;size[1] = lenY;size[2] = lenZ;
				List<InvertLineEntity> invertLineList = new ArrayList<>();
				for(Map.Entry<Integer, List<LineEntity>> m:YZMap.entrySet()) {
					for(LineEntity l:m.getValue()) {
						InvertLineEntity inLineEntity = new InvertLineEntity();
						inLineEntity.setBegin(l.getBegin());
						inLineEntity.setEnd(l.getEnd());
						inLineEntity.setType("YZ");
						inLineEntity.setStackNum(m.getKey());
						invertLineList.add(inLineEntity);
					}
				}
				
				for(Map.Entry<Integer, List<LineEntity>> m:XZMap.entrySet()) {
					for(LineEntity l:m.getValue()) {
						InvertLineEntity inLineEntity = new InvertLineEntity();
						inLineEntity.setBegin(l.getBegin());
						inLineEntity.setEnd(l.getEnd());
						inLineEntity.setType("XZ");
						inLineEntity.setStackNum(m.getKey());
						invertLineList.add(inLineEntity);
					}
				}
				
				for(Map.Entry<Integer, List<LineEntity>> m:XYMap.entrySet()) {
					for(LineEntity l:m.getValue()) {
						InvertLineEntity inLineEntity = new InvertLineEntity();
						inLineEntity.setBegin(l.getBegin());
						inLineEntity.setEnd(l.getEnd());
						inLineEntity.setType("XY");
						inLineEntity.setStackNum(m.getKey());
						invertLineList.add(inLineEntity);
					}
				}
				
				ThreadPool.getThreadPool().execute(()->{
					try {
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document document = dBuilder.parse(new File(GlobalValue.URL+"/generate/dataset.xml"));
						document.getDocumentElement().normalize();

						NodeList brainMapList = document.getElementsByTagName("brainMap");
						Element brainMapEle = (Element)brainMapList.item(0);
						if(brainMapEle.hasAttribute("coarseRegistration")) brainMapEle.removeAttribute("coarseRegistration");
						
						if(brainMapEle.getElementsByTagName("coarseRegistration").getLength()>0){
							brainMapEle.removeAttribute("coarseRegistration");
						}
						Element coarseRegistrationEle = document.createElement("coarseRegistration");
						Element invertEle = document.createElement("invert");
						Element XYEle = document.createElement("XY");
						Element XZEle = document.createElement("XZ");
						Element YZEle = document.createElement("YZ");
						String XYStr = "",XZStr = "",YZStr = "";
						for(InvertLineEntity i:invertLineList){
							String tempStr = i.getBegin().x+" "+i.getBegin().y+" "+i.getStackNum()+","+
									i.getEnd().x+" "+i.getEnd().y+" "+i.getStackNum()+";";
							switch (i.getType()) {
							case "XY":XYStr+=tempStr;break;
							case "XZ":XZStr+=tempStr;break;
							case "YZ":YZStr+=tempStr;break;
							default:break;
							}
						}
						XYEle.setTextContent(XYStr.length()>0?XYStr.substring(0, XYStr.length()-1):XYStr);
						XZEle.setTextContent(XZStr.length()>0?XZStr.substring(0, XZStr.length()-1):XZStr);
						YZEle.setTextContent(YZStr.length()>0?YZStr.substring(0, YZStr.length()-1):YZStr);
						invertEle.appendChild(XYEle);
						invertEle.appendChild(XZEle);
						invertEle.appendChild(YZEle);
						coarseRegistrationEle.appendChild(invertEle);
						brainMapEle.appendChild(coarseRegistrationEle);
						
						document.getDocumentElement().normalize();
			            TransformerFactory transformerFactory = TransformerFactory.newInstance();
			            Transformer transformer = transformerFactory.newTransformer();
			            DOMSource source = new DOMSource(document);
			            StreamResult result = new StreamResult(new File(GlobalValue.URL+"/generate/dataset.xml"));
			            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			            transformer.transform(source, result);
					} catch (Exception e2) {}
					
					double[][][] thumbMask = InvertTools.invert(invertLineList, size);
					invertImage = NewImage.createByteImage("invertImage", lenX, lenY, lenZ, NewImage.FILL_BLACK);
					ImageStack invertImageStack = invertImage.getImageStack();
					int change = 0;
					for(int i=0;i<size[2];i++){
				 		for(int j=0;j<size[1];j++){
				 			for(int k=0;k<size[0];k++){
				 				if(downSampleImageMaskStack.getVoxel(k, j, i)>0&&(int)thumbMask[i][j][k]>0)
				 					change = 255-(int)downSampleImageStack.getVoxel(k, j, i);
				 				else
				 					change = (int)downSampleImageStack.getVoxel(k, j, i);
				 				
				 				invertImageStack.setVoxel(k, j, i, change);
				 			}
				 		}
				 	}
				 	invertImage.setStack(invertImageStack);
				 	invertImage.show();
				});
				
			}
		});
		
		deleteButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				int currentSlice = downSampleImageRGB.getCurrentSlice();
				int XYNum = XYMap.containsKey(currentSlice)?XYMap.get(currentSlice).size():0,
					YZNum = YZMap.containsKey(currentSlice)?YZMap.get(currentSlice).size():0,
					XZNum = XZMap.containsKey(currentSlice)?XZMap.get(currentSlice).size():0;
				
				XYMap.put(currentSlice, new ArrayList<>());
				XZMap.put(currentSlice, new ArrayList<>());
				YZMap.put(currentSlice, new ArrayList<>());
				
				xYLabel.setText(Integer.parseInt(xYLabel.getText())-XYNum+"");
				yZLabel.setText(Integer.parseInt(yZLabel.getText())-YZNum+"");
				xZLabel.setText(Integer.parseInt(xZLabel.getText())-XZNum+"");
				
				downSampleImageRGBStack.setProcessor(downSampleImageStack.getProcessor(currentSlice).convertToRGB(), currentSlice);
				downSampleImageRGB.setStack(downSampleImageRGBStack);
				downSampleImageRGB.show();
			}
		});
		
		pcCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean pcMethod = pcCheck.isSelected();
				if(pcMethod) PCField.setEditable(true);
				else PCField.setEditable(false);
			}
		});
		
		invertCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean invertMethod = invertCheck.isSelected();
				
				if(GlobalValue.downSampleImage==null) {
					if((new File(GlobalValue.URL+"/registration/coarse/downSampleImage.tif")).exists()) {
						GlobalValue.downSampleImage = new ImagePlus(GlobalValue.URL+"/registration/coarse/downSampleImage.tif");
					}else {
						LogIn.show("have not down sample");
						JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
						invertCheck.setSelected(false);
						return;
					}
				}
				
				lenX = GlobalValue.downSampleImage.getWidth();
				lenY = GlobalValue.downSampleImage.getHeight();
				lenZ = GlobalValue.downSampleImage.getStackSize();
				downSampleImageStack = GlobalValue.downSampleImage.getImageStack();
				
				if(new File(GlobalValue.URL+"/registration/coarse/downSampleImageMask.tif").exists()){
					downSampleImageMask = new ImagePlus(GlobalValue.URL+"/registration/coarse/downSampleImageMask.tif");
					downSampleImageMaskStack = downSampleImageMask.getImageStack();
				}else{
					downSampleImageMask = NewImage.createByteImage("mask", lenX, lenY, lenZ, NewImage.FILL_BLACK);
					downSampleImageMaskStack = downSampleImageMask.getImageStack();
					AutoThresholder auto = new AutoThresholder();
					
					int maxThread = GlobalValue.MAXTHREAD, batch = lenZ/maxThread+(lenZ%maxThread>0?1:0);
					for(int b=0;b<batch;b++) {
						int num = (lenZ-maxThread*b)>maxThread?maxThread:(lenZ-maxThread*b);
						CountDownLatch count1 = new CountDownLatch(num);
						for(int z=b*maxThread+1;z<=b*maxThread+num;z++) {
							final int zz = z;
							ThreadPool.getThreadPool().execute(()->{
								int[] hist = downSampleImageStack.getProcessor(zz).getHistogram();
								int begin = 0,end = hist.length;
								for(int i=0;i<end;i++){
									if(hist[i]>0) break;
									else begin++;
								}
								for(int i=end-1;i>=0;i--){
									if(hist[i]>0) break;
									else end--;
								}
								if(end-begin<=3){
									count1.countDown();
									return;
								}
								int threshold = auto.getThreshold("Huang", hist);
								for(int y=0;y<lenY;y++){
									for(int x=0;x<lenX;x++){
										if(downSampleImageStack.getVoxel(x, y, zz-1)>threshold)
											downSampleImageMaskStack.setVoxel(x, y, zz-1, 255);
									}
								}
								count1.countDown();
							});
						}
						try {
							count1.await();
						} catch (Exception e) {}
					}
					
					
					downSampleImageMask.setStack(downSampleImageMaskStack);
				}
				
				XYMap = new HashMap<>();
				XZMap = new HashMap<>();
				YZMap = new HashMap<>();
				
				if(invertMethod) {
					downSampleImageRGB = NewImage.createRGBImage("operation", lenX, lenY, lenZ, NewImage.FILL_BLACK);
					downSampleImageRGBStack = downSampleImageRGB.getImageStack();
					
					int maxThread = GlobalValue.MAXTHREAD, batch = lenZ/maxThread+(lenZ%maxThread>0?1:0);
					for(int b=0;b<batch;b++) {
						int num = (lenZ-maxThread*b)>maxThread?maxThread:(lenZ-maxThread*b);
						CountDownLatch count2 = new CountDownLatch(num);
						for(int z=b*maxThread+1;z<=b*maxThread+num;z++) {
							final int zz = z;
							ThreadPool.getThreadPool().execute(()->{
								downSampleImageRGBStack.setProcessor(downSampleImageStack.getProcessor(zz).convertToRGB(), zz);
								count2.countDown();
							});
						}
						try {
							count2.await();
						} catch (Exception e) {}
					}
					
					downSampleImageRGB.setStack(downSampleImageRGBStack);
					
			        try {
			        	DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			            DocumentBuilder db=factory.newDocumentBuilder();
			            Document document = db.parse(new File(GlobalValue.URL+"/generate/dataset.xml"));
						document.getDocumentElement().normalize();
						Element brainMapEle = (Element)document.getElementsByTagName("brainMap").item(0);
						if(brainMapEle.getElementsByTagName("coarseRegistration").getLength()>0){
							int n = JOptionPane.showConfirmDialog(null, "Do you want load param?", "load",JOptionPane.YES_NO_OPTION); 
							if(n==0){
								int loc = brainMapEle.getElementsByTagName("coarseRegistration").getLength();
								Element coarseRegistrationEle = (Element)brainMapEle.getElementsByTagName("coarseRegistration").item(loc-1);
								Element invertEle = (Element)coarseRegistrationEle.getElementsByTagName("invert").item(0);
								String XYStr = ((Element)invertEle.getElementsByTagName("XY").item(0)).getTextContent(),
										XZStr = ((Element)invertEle.getElementsByTagName("XZ").item(0)).getTextContent(),
										YZStr = ((Element)invertEle.getElementsByTagName("YZ").item(0)).getTextContent();
								String[] lineXYList = XYStr.length()>0?XYStr.split(";"):null,
										lineXZList = XZStr.length()>0?XZStr.split(";"):null,
										lineYZList = YZStr.length()>0?YZStr.split(";"):null;
								
								downSampleImageRGBStack = downSampleImageRGB.getImageStack();		
								
								xYLabel.setText(lineXYList==null?"0":String.valueOf(lineXYList.length));
								xZLabel.setText(lineXZList==null?"0":String.valueOf(lineXZList.length));
								yZLabel.setText(lineYZList==null?"0":String.valueOf(lineYZList.length));
								if(lineXYList!=null)
								for(String lineXYStr:lineXYList){
									String[] lineStr = lineXYStr.split(",");
									String[] beginStr = lineStr[0].split(" "),endStr = lineStr[1].split(" ");
									int[] begin = new int[3];
									int[] end = new int[3];
									for(int i=0;i<3;i++){
										begin[i] = Integer.valueOf(beginStr[i]);
										end[i] = Integer.valueOf(endStr[i]);
									}
									if(!XYMap.containsKey(begin[2])){
										XYMap.put(begin[2], new ArrayList<>());
									}
									LineEntity lineEntity = new LineEntity();
									lineEntity.setBegin(new Point(begin[0],begin[1]));
									lineEntity.setEnd(new Point(end[0],end[1]));
									XYMap.get(begin[2]).add(lineEntity);
									
									ColorProcessor colorProcessor = downSampleImageRGBStack.getProcessor(begin[2]).convertToColorProcessor();
									colorProcessor = ArrowTools.drawLine(lineEntity.getBegin().x, lineEntity.getBegin().y,
											lineEntity.getEnd().x, lineEntity.getEnd().y, colorProcessor, 1);
									
									downSampleImageRGBStack.setProcessor(colorProcessor, begin[2]);
								}
								if(lineXZList!=null)
								for(String lineXZStr:lineXZList){
									String[] lineStr = lineXZStr.split(",");
									String[] beginStr = lineStr[0].split(" "),endStr = lineStr[1].split(" ");
									int[] begin = new int[3];
									int[] end = new int[3];
									for(int i=0;i<3;i++){
										begin[i] = Integer.valueOf(beginStr[i]);
										end[i] = Integer.valueOf(endStr[i]);
									}
									if(!XZMap.containsKey(begin[2])){
										XZMap.put(begin[2], new ArrayList<>());
									}
									LineEntity lineEntity = new LineEntity();
									lineEntity.setBegin(new Point(begin[0],begin[1]));
									lineEntity.setEnd(new Point(end[0],end[1]));
									XZMap.get(begin[2]).add(lineEntity);
									
									ColorProcessor colorProcessor = downSampleImageRGBStack.getProcessor(begin[2]).convertToColorProcessor();
									
									colorProcessor = ArrowTools.drawLine(lineEntity.getBegin().x, lineEntity.getBegin().y,
											lineEntity.getEnd().x, lineEntity.getEnd().y, colorProcessor, 2);
									
									downSampleImageRGBStack.setProcessor(colorProcessor, begin[2]);
								}
								if(lineYZList!=null)
								for(String lineYZStr:lineYZList){
									String[] lineStr = lineYZStr.split(",");
									String[] beginStr = lineStr[0].split(" "),endStr = lineStr[1].split(" ");
									int[] begin = new int[3];
									int[] end = new int[3];
									for(int i=0;i<3;i++){
										begin[i] = Integer.valueOf(beginStr[i]);
										end[i] = Integer.valueOf(endStr[i]);
									}
									if(!YZMap.containsKey(begin[2])){
										YZMap.put(begin[2], new ArrayList<>());
									}
									LineEntity lineEntity = new LineEntity();
									lineEntity.setBegin(new Point(begin[0],begin[1]));
									lineEntity.setEnd(new Point(end[0],end[1]));
									YZMap.get(begin[2]).add(lineEntity);
									
									ColorProcessor colorProcessor = downSampleImageRGBStack.getProcessor(begin[2]).convertToColorProcessor();
									
									colorProcessor = ArrowTools.drawLine(lineEntity.getBegin().x, lineEntity.getBegin().y,
											lineEntity.getEnd().x, lineEntity.getEnd().y, colorProcessor, 3);
									
									downSampleImageRGBStack.setProcessor(colorProcessor, begin[2]);
								}
								
							}
						}
						downSampleImageRGB.setStack(downSampleImageRGBStack);
						downSampleImageRGB.show();
						downSampleImageRGB.getCanvas().addMouseListener(mouseListener);
			        }catch(Exception e){}
				}
				else {
					if(invertImage!=null) invertImage.close();
					xYLabel.setText("0");
					xZLabel.setText("0");
					yZLabel.setText("0");
					
					downSampleImageRGB.close();
				}
				if(invertMethod) invertField.setEditable(true);
				else invertField.setEditable(false);
			}
		});
	}
	
	public JPanel location() {
		JPanel outPanel = new JPanel();
		
		outPanel.setLayout(null);
		
		int beginX = 30,beginY = 20,high = 20;
		JLabel featureLabel = new JLabel("feature:");
		JLabel invertChooseLabel = new JLabel("invert:");
		invertCheck = new JCheckBox("", false);
		invertField = new JTextField("1");
		JLabel PCChooseLabel = new JLabel("PC:");
		pcCheck = new JCheckBox("", false);
		PCField = new JTextField("1");
		JLabel rowLabel = new JLabel("raw:");
		rowField = new JTextField("1");
		featureLabel.setBounds(beginX, beginY, 60, high);
		invertChooseLabel.setBounds(beginX+=60, beginY, 50, high);
		invertCheck.setBounds(beginX+=50, beginY, 20, high);
		invertField.setBounds(beginX+=20, beginY, 30, high);
		PCChooseLabel.setBounds(beginX+=50, beginY, 30, high);
		pcCheck.setBounds(beginX+=30, beginY, 20, high);
		PCField.setBounds(beginX+=20, beginY, 30, high);
		rowLabel.setBounds(beginX+=50, beginY, 50, high);
		rowField.setBounds(beginX+=50, beginY, 30, high);
		outPanel.add(featureLabel);
		outPanel.add(invertChooseLabel);
		outPanel.add(invertCheck);
		outPanel.add(invertField);
		outPanel.add(PCChooseLabel);
		outPanel.add(pcCheck);
		outPanel.add(PCField);
		outPanel.add(rowLabel);
		outPanel.add(rowField);
		invertField.setEditable(false);
		PCField.setEditable(false);
		
		beginX = 30;beginY += 40;
		JLabel modelLabel = new JLabel("model:");
		rigidCheck = new JCheckBox("rigid", true);
		affineCheck = new JCheckBox("affine",true);
		bsplineCheck = new JCheckBox("bspline", true);
		JLabel iterLabel = new JLabel("iter:");
		iterField = new JTextField("800");
		modelLabel.setBounds(beginX, beginY, 60, high);
		rigidCheck.setBounds(beginX+=60, beginY, 60, high);
		affineCheck.setBounds(beginX+=60, beginY, 60, high);
		bsplineCheck.setBounds(beginX+=60, beginY, 70, high);
		iterLabel.setBounds(beginX+=80, beginY, 40, high);
		iterField.setBounds(beginX+=40, beginY, 40, high);
		outPanel.add(modelLabel);
		outPanel.add(rigidCheck);
		outPanel.add(affineCheck);
		outPanel.add(bsplineCheck);
		outPanel.add(iterLabel);
		outPanel.add(iterField);
		
		beginX = 30;beginY += 40;
        JLabel invertLabel = new JLabel("invert:");
		JLabel xY = new JLabel("XY:");
		xYLabel = new JLabel("0");
		JLabel xZ = new JLabel("XZ:");
		xZLabel = new JLabel("0");
		JLabel yZ = new JLabel("YZ:");
		yZLabel = new JLabel("0");
		changeModel = new Button("YZ");
		deleteButton = new Button("delete");
		invertButton = new Button("invert");
		invertLabel.setBounds(beginX, beginY, 60, high);
		xY.setBounds(beginX+=60, beginY, 35, high);
		xYLabel.setBounds(beginX+=35, beginY, 35, high);
		xZ.setBounds(beginX+=35, beginY, 35, high);
		xZLabel.setBounds(beginX+=35, beginY, 35, high);
		yZ.setBounds(beginX+=35, beginY, 35, high);
		yZLabel.setBounds(beginX+=35, beginY, 35, high);
		changeModel.setBounds(beginX+=35, beginY, 35, high);
		deleteButton.setBounds(beginX+=45, beginY, 40, high);
		invertButton.setBounds(beginX+=45, beginY, 40, high);
		outPanel.add(invertLabel);
		outPanel.add(xY);
		outPanel.add(xYLabel);
		outPanel.add(xZ);
		outPanel.add(xZLabel);
		outPanel.add(yZ);
		outPanel.add(yZLabel);
		outPanel.add(changeModel);
		outPanel.add(deleteButton);
		outPanel.add(invertButton);
		
		beginX = 200;beginY += 40;
		okButton = new Button("ok");
		okButton.setBounds(beginX, beginY, 35, high);
		outPanel.add(okButton);
		
		mouseListener = this;
		invertState = 3;
		event();
		return outPanel;
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		lineEntity = new LineEntity();
		lineEntity.setBegin(downSampleImageRGB.getCanvas().getCursorLoc());
	}
	public void mouseReleased(MouseEvent e) {
		int currentSlice = downSampleImageRGB.getCurrentSlice();
		Point tempEnd = downSampleImageRGB.getCanvas().getCursorLoc();
		Point tempBegin = lineEntity.getBegin();
		int len = (int)Math.pow((double)(tempEnd.x-tempBegin.x), 2) + (int)Math.pow((double)(tempEnd.y-tempBegin.y), 2);
		len = (int)Math.sqrt((double)len);
		if(len<10) return;
		lineEntity.setEnd(downSampleImageRGB.getCanvas().getCursorLoc());
		
		switch (invertState) {
		case 1: if(!XYMap.containsKey(currentSlice)) { XYMap.put(currentSlice, new ArrayList<>()); }
				XYMap.get(currentSlice).add(lineEntity);
				xYLabel.setText(Integer.parseInt(xYLabel.getText())+1+"");
				break;
		case 2:  if(!XZMap.containsKey(currentSlice)) { XZMap.put(currentSlice, new ArrayList<>()); }
				XZMap.get(currentSlice).add(lineEntity);
				xZLabel.setText(Integer.parseInt(xZLabel.getText())+1+"");
				break;
		case 3:  if(!YZMap.containsKey(currentSlice)) { YZMap.put(currentSlice, new ArrayList<>()); }
				YZMap.get(currentSlice).add(lineEntity);
				yZLabel.setText(Integer.parseInt(yZLabel.getText())+1+"");
				break;
		}
		
		downSampleImageRGBStack = downSampleImageRGB.getImageStack();
		ColorProcessor colorProcessor = downSampleImageRGBStack.getProcessor(currentSlice).convertToColorProcessor();
		
		colorProcessor = ArrowTools.drawLine(lineEntity.getBegin().x, lineEntity.getBegin().y,
				lineEntity.getEnd().x, lineEntity.getEnd().y, colorProcessor, invertState);
		
		downSampleImageRGBStack.setProcessor(colorProcessor, downSampleImageRGB.getCurrentSlice());
		downSampleImageRGB.setStack(downSampleImageRGBStack);
		downSampleImageRGB.deleteRoi();
		downSampleImageRGB.show();
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setSize(500, 300);
		f.add(new CoarseRegistration().location());
		f.setVisible(true);
	}

}
