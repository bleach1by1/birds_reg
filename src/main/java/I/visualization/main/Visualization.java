package I.visualization.main;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bitplane.xt.BPImarisLib;

import I.plugin.GlobalValue;
import I.plugin.LogIn;
import I.plugin.ThreadPool;
import I.utils.ExcelTools;
import I.utils.FileCopyUtils;
import I.visualization.tools.GetBox3DExcel;
import I.visualization.tools.ImarisTools;
import I.visualization.tools.UpSampleTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;

/**
 * visualization module 
 * @author zengweilin
 *
 */

public class Visualization {

	private Visualization() {}
	
	public static boolean STOP = false;
	
	public static ImagePlus annotationImage = null;
	
	private static Visualization visualization = null;
	
	public static Visualization getVisualization() {
		if(visualization==null) {
			synchronized (Visualization.class) {
				if(visualization==null) {
					visualization = new Visualization();
				}
			}
		}
		return visualization;
	}
	
	private JTextField xSize,ySize,zSize;
	private Button upSample;
	
	private JTextField excelUpText;
	private Button upload;
	
	private JLabel judge;
	private Button connectImarisButton;
	private Button generateSurfaceButton,stopButton;
	
	private int imarisId = -1;
	
	public JPanel location() {
		JPanel outPanel = new JPanel(){
			private static final long serialVersionUID = 1L;
        	   public Dimension getPreferredSize() {
        	     return new Dimension(500, 200);
        	   }
        }; 
		
		outPanel.setLayout(null);
		
		JLabel upSampleLabel = new JLabel("upSample:");
		
		JLabel xSizeLabel = new JLabel("X (pixel):");
		JLabel xSizeUm = new JLabel("um");
		JLabel ySizeLabel = new JLabel("Y (pixel):");
		JLabel ySizeUm = new JLabel("um");
		JLabel zSizeLabel = new JLabel("Z (pixel):");
		JLabel zSizeUm = new JLabel("um");
		xSize = new JTextField(4);
		ySize = new JTextField(4);
		zSize = new JTextField(4);
		upSample = new Button("up");
		
		JLabel downloadLabel = new JLabel("regionUrl:");
		excelUpText = new JTextField(15);
		upload = new Button("upload");
		
        Image image = Toolkit.getDefaultToolkit().getImage("D:/3.png");
        image = image.getScaledInstance(8, 8, Image.SCALE_DEFAULT);
		
        JLabel imarisLabel = new JLabel("imaris:");
        judge = new JLabel("False");
        connectImarisButton = new Button("connect");
        generateSurfaceButton = new Button("generate");
        stopButton = new Button("stop");
		
        int beginX = 5,beginY = 20,high = 20;
		upSampleLabel.setBounds(beginX, beginY, 60, high);
		xSizeLabel.setBounds(beginX+=65, beginY, 60, high);
		xSize.setBounds(beginX+=60, beginY, 40, high);
		xSizeUm.setBounds(beginX+=40, beginY, 20, high);
		ySizeLabel.setBounds(beginX+=10, beginY, 60,high);
		ySize.setBounds(beginX+=60, beginY, 40, high);
		ySizeUm.setBounds(beginX+=40, beginY, 20, high);
		zSizeLabel.setBounds(beginX+=10,beginY,60,high);
		zSize.setBounds(beginX+=60, beginY, 40, high);
		zSizeUm.setBounds(beginX+=40, beginY, 20, high);
		upSample.setBounds(beginX+=10, beginY, 30, high);
		
		
		beginX = 90;beginY += 40;high = 20;
		downloadLabel.setBounds(beginX, beginY, 60, high);
		excelUpText.setBounds(beginX+=60, beginY, 150, high);
		upload.setBounds(beginX+=160, beginY, 50, high);
		
		beginX = 70;beginY += 40;high = 20;
		imarisLabel.setBounds(beginX, beginY, 40, high);
		judge.setBounds(beginX+=50, beginY, 40, high);
		connectImarisButton.setBounds(beginX+=50, beginY, 50, high);
		generateSurfaceButton.setBounds(beginX+=60, beginY, 55, high);
		stopButton.setBounds(beginX+=60, beginY, 45, high);
		
		outPanel.add(upSampleLabel);
		outPanel.add(xSizeLabel);
		outPanel.add(xSize);
		outPanel.add(ySizeLabel);
		outPanel.add(ySize);
		outPanel.add(zSizeLabel);
		outPanel.add(zSize);
		outPanel.add(upSample);
		
		outPanel.add(downloadLabel);
		outPanel.add(excelUpText);
		outPanel.add(upload);
		
		outPanel.add(imarisLabel);
		outPanel.add(judge);
		outPanel.add(connectImarisButton);
		outPanel.add(generateSurfaceButton);
		outPanel.add(stopButton);
		event();
		return outPanel;
	}
	
	private void event(){
		
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				STOP = true;
			}
		});
		
		connectImarisButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BPImarisLib vImarisLib = new BPImarisLib();
				int imarisIdGet = ImarisTools.GetObjectId(vImarisLib);
				if(imarisIdGet>=0) {
					judge.setText("True");
				}else {
					imarisId = imarisIdGet;
					LogIn.show("imaris id is:"+imarisId);
				}
			}
		});
		
		upload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				excelUpText.setText(URL);
			}
		});
		
		generateSurfaceButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				ThreadPool.getThreadPool().execute(()->{
					try {
						String[][] levelStr = ExcelTools.getExcelTools().getData(new File(GlobalValue.URL+"/cache/level.xls"), 2);
						String[][] rgbStr = ExcelTools.getExcelTools().getData(new File(GlobalValue.URL+"/cache/rgb_name.xls"), 0);
						
						Map<Integer, Integer> colorMap = new HashMap<>();
						Map<String, Integer> name2IntMap = new HashMap<>();
						Map<Integer, List<Integer>> treeBackMap = new HashMap<>();
						Map<Integer, List<Integer>> treeBeforeMap = new HashMap<>();
						Map<Integer, String> int2NameMap = new HashMap<>();
						
						String[][] boxStr = ExcelTools.getExcelTools().getData(new File(GlobalValue.URL+"/visual/wholeBrain.xls"), 0);
						Map<Integer, int[][]> box3DMap = new HashMap<>();
						for(String[] str:boxStr) {
							if(str[4]==null||str[4]=="") break;
							int[][] temp = new int[][] {{Integer.valueOf(str[1]),Integer.valueOf(str[2]),Integer.valueOf(str[3]),0,
								Integer.valueOf(str[4]),Integer.valueOf(str[5]),Integer.valueOf(str[6]),0}};
								box3DMap.put(Integer.valueOf(str[0]), temp);
						}
						
						for(String[] str:rgbStr) {
							name2IntMap.put(str[1], Integer.valueOf(str[0]));
							int2NameMap.put(Integer.valueOf(str[0]), str[1]);
							int value = Integer.valueOf(str[2])+Integer.valueOf(str[3])*256+Integer.valueOf(str[4])*256*256;
							colorMap.put(Integer.valueOf(str[0]),value);
						}
						
						for(String[] str:levelStr){
							int val = Integer.valueOf(str[1]);
							String[] beforeStr = str[6].split("/");
							List<Integer> tempList = new ArrayList<>();
							for(int i=0;i<beforeStr.length-1;i++){
								if(!beforeStr[i].equals("")) tempList.add(Integer.valueOf(beforeStr[i]));
							}
							if(!int2NameMap.containsKey(val)) int2NameMap.put(val, str[3]);
							if(!name2IntMap.containsKey(str[3])) name2IntMap.put(str[3], val);
							treeBeforeMap.put(val, tempList);
						}
						for(String[] str:levelStr){
							int depth = Integer.valueOf(str[5]),val = Integer.valueOf(str[1]);
							if(depth==0&&!treeBackMap.containsKey(val)){
								treeBackMap.put(val, new ArrayList<>());continue;
							}
							int parentVal = Integer.valueOf(str[4]);
							if(!treeBackMap.containsKey(parentVal)){
								treeBackMap.put(parentVal, new ArrayList<>());
							}
							treeBackMap.get(parentVal).add(val);
						}
						
						BPImarisLib vImarisLib = new BPImarisLib();
						imarisId = ImarisTools.GetObjectId(vImarisLib);
						Imaris.IApplicationPrx application = vImarisLib.GetApplication(imarisId);
						Imaris.IDataSetPrx  dataSet = application.GetDataSet();
						Imaris.IDataContainerPrx vGroup = 
						  application.GetFactory().CreateDataContainer();
						vGroup.SetName("root");
						Imaris.IDataContainerPrx scence =  application.GetSurpassScene();
						scence.AddChild(vGroup, -1);
						
						Queue<Integer> tempInt = new LinkedList<>();
						Queue<Imaris.IDataContainerPrx> tempFile = new LinkedList<>();
						//whether excel is uploaded 
						if(excelUpText.getText().length()==0) {
							//root's region
							for(Integer i:treeBackMap.get(997)) {
								tempInt.add(i);
								Imaris.IDataContainerPrx tempVGroup = 
										  application.GetFactory().CreateDataContainer();
								tempVGroup.SetName(int2NameMap.get(i));
								vGroup.AddChild(tempVGroup, -1);
								tempFile.add(tempVGroup);
							}
						}else {
							String[][] tempName = ExcelTools.getExcelTools().getData(new File(excelUpText.getText()), 0);
							for(String[] str:tempName) {
								int val = name2IntMap.get(str[0]);
								if(treeBackMap.get(val)==null) {
									if(!box3DMap.containsKey(val)) continue;
									float a0 = 0,a1=10,a4=(float)val-(float)0.5,a5=(float)val+(float)0.5;
									Imaris.ISurfacesPrx surface =  application.GetImageProcessing().
											DetectSurfacesWithUpperThreshold(dataSet, box3DMap.get(val), 0, a1, a0, true, false, a4, 
													true, false, a5, "");
									
									box3DMap.remove(val);
									
									if(!colorMap.containsKey(val)) continue;
									surface.begin_SetColorRGBA(colorMap.get(val));
									System.out.println(colorMap.get(val));
									surface.SetName(int2NameMap.get(val));
									surface.SetVisible(true);
									vGroup.AddChild(surface, -1);
									scence.AddChild(vGroup, -1);
								}else {
									tempInt.add(val);
									Imaris.IDataContainerPrx tempVGroup = 
											  application.GetFactory().CreateDataContainer();
									tempVGroup.SetName(int2NameMap.get(val));
									vGroup.AddChild(tempVGroup, -1);
									tempFile.add(tempVGroup);
								}
							}
						}
						
						while(!tempInt.isEmpty()&&excelUpText.getText().length()==0) {
							int preInt = tempInt.poll();
							Imaris.IDataContainerPrx preVGroup = tempFile.poll();
							for(Integer i:treeBackMap.get(preInt)) {
								if(STOP) { STOP = false;return; }
								if(treeBackMap.get(i)==null) {
									if(!box3DMap.containsKey(i)) continue;
									float a0 = 0,a1=10,a4=(float)i-(float)0.5,a5=(float)i+(float)0.5;
									Imaris.ISurfacesPrx surface =  application.GetImageProcessing().
											DetectSurfacesWithUpperThreshold(dataSet, box3DMap.get(i), 0, a1, a0, true, false, a4, 
													true, false, a5, "");
									
									box3DMap.remove(i);
									
									if(!colorMap.containsKey(i)) continue;
									surface.begin_SetColorRGBA(colorMap.get(i));
									System.out.println(colorMap.get(i));
									surface.SetName(int2NameMap.get(i));
									surface.SetVisible(true);
									preVGroup.AddChild(surface, -1);
									scence.AddChild(vGroup, -1);
								}else {
									tempInt.add(i);
									Imaris.IDataContainerPrx tempVGroup = 
											  application.GetFactory().CreateDataContainer();
									tempVGroup.SetName(int2NameMap.get(i));
									preVGroup.AddChild(tempVGroup, -1);
									tempFile.add(tempVGroup);
								}
							}
						}
						
						if(box3DMap.size()>0) {
							Imaris.IDataContainerPrx nilVGroup = 
									  application.GetFactory().CreateDataContainer();
							nilVGroup.SetName("nil");
							vGroup.AddChild(nilVGroup, -1);
							for(Map.Entry<Integer, int[][]> m:box3DMap.entrySet()) {
								if(STOP) { STOP = false;return; }
								int i = m.getKey();
								float a0 = 0,a1=10,a4=(float)i-(float)0.5,a5=(float)i+(float)0.5;
								Imaris.ISurfacesPrx surface =  application.GetImageProcessing().
										DetectSurfacesWithUpperThreshold(dataSet, box3DMap.get(i), 0, a1, a0, true, false, a4, 
												true, false, a5, "");
								if(!colorMap.containsKey(i)) continue;
								System.out.println(colorMap.get(i));
								if(int2NameMap.containsKey(i)) {
									surface.SetColorRGBA(colorMap.get(i));
									surface.SetName(int2NameMap.get(i));
								}
								else {
									surface.SetName("nil");
								}
								surface.SetVisible(true);
								nilVGroup.AddChild(surface, -1);
								scence.AddChild(vGroup, -1);
							}
						}
						
					} catch (Exception e2) {
						LogIn.error(e2.getMessage());
					}
				});
			}
		});
		
		upSample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LogIn.show("begin upsample...");
				ThreadPool.getThreadPool().execute(()->{
					int lenX = 0,lenY = 0,lenZ = 0,upZ = 0;
					String brainNum = "",altasNum="";
					try {
						DocumentBuilderFactory factorys = DocumentBuilderFactory.newInstance();
			            DocumentBuilder db=factorys.newDocumentBuilder();
			            Document document = db.parse(new File(GlobalValue.URL+"/generate/dataset.xml"));
						document.getDocumentElement().normalize();
						Element brainMapEle = (Element)document.getElementsByTagName("brainMap").item(0);
						Element downSampleEle = (Element)brainMapEle.getElementsByTagName("downSample").item(0);
						Element orgImageEle = (Element)downSampleEle.getElementsByTagName("orgImage").item(0);
						Element tempImageEle = (Element)downSampleEle.getElementsByTagName("tempImage").item(0);
						Element orgImageDownEle = (Element)orgImageEle.getElementsByTagName("orgImageDown").item(0);
						Element tempImageDownEle = (Element)tempImageEle.getElementsByTagName("tempImageDown").item(0);
						Element orgImageSizeEle = (Element)orgImageEle.getElementsByTagName("orgImageSize").item(0);
						brainNum = orgImageDownEle.getTextContent();
						altasNum = tempImageDownEle.getTextContent();
						String[] temp = orgImageSizeEle.getTextContent().split(",");
						upZ = Integer.valueOf(temp[temp.length-1]);
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(null, "Please do the previous operation", "error",JOptionPane.PLAIN_MESSAGE);
					}
		            lenX = Integer.parseInt(xSize.getText().trim());
		            lenY = Integer.parseInt(ySize.getText().trim());
		            lenZ = Integer.parseInt(zSize.getText().trim());
		            
		            if(!new File(GlobalValue.URL+"/cache/rgb_name.xls").exists()) {
		            	FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("rgb_name.xls"), GlobalValue.URL+"/cache/rgb_name.xls");
		            }
		            if(!new File(GlobalValue.URL+"/cache/level.xls").exists()) {
		            	FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("level.xls"), GlobalValue.URL+"/cache/level.xls");
		            }
		            
		            
		            ImagePlus upAnnotationTemp = UpSampleTools.zRestoreShort1(brainNum, altasNum, annotationImage, upZ);
		            
		            ImagePlus outAnnotation = NewImage.createImage("outAnnotation", lenX, lenY, lenZ,16, NewImage.FILL_BLACK);
		            ImageStack upAnnotationTempStack = upAnnotationTemp.getImageStack(),
		            		outAnnotationStack = outAnnotation.getImageStack();
		            
		            double scale = (double)upZ/(double)lenZ;
		            for(int z=1;z<=lenZ;z++) {
		            	outAnnotationStack.setProcessor(upAnnotationTempStack.getProcessor((int)Math.ceil(scale*z-scale/2.0)).resize(lenX, lenY, false)
		            			, z);
		            }
		            outAnnotation.setStack(outAnnotationStack);
		            new File(GlobalValue.URL+"/visual").mkdirs();
		            GetBox3DExcel.getBox3DExcel(outAnnotation, GlobalValue.URL+"/visual/wholeBrain.xls");
		            new FileSaver(outAnnotation).saveAsTiff(GlobalValue.URL+"/visual/annotation.tif");
		            LogIn.show("end upsample...");
				});
				
			}
		});
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setSize(500, 200);
		f.add(new Visualization().location());
		f.setVisible(true);
	}

}
