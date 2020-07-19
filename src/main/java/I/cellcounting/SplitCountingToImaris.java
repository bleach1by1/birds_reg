package I.cellcounting;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.bitplane.xt.BPImarisLib;

import I.utils.ExcelTools;
import I.visualization.tools.ImarisTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;

public class SplitCountingToImaris implements PlugIn{
	public static boolean CONNECT = false, STOP = false;
	public static float VOXELSIZE = 20,PHYSICALRADIUS = 2;
	public static float RADIUS = PHYSICALRADIUS*VOXELSIZE;

	public JFrame frame;
	
	public JLabel imarisCellJudge;
	public Button imarisConnect,imageChoose,excelChoose,levelChoose,ok,stop,export;
	public JTextField voxelsizeField, physicField, imageField,excelField,levelField;
	
	public Map<Integer, List<int[]>> regionSpotsMap;
	
	private void location() {
		frame = new JFrame("splitCountingToImaris");
		frame.setBounds(300, 400, 450, 210);
		frame.setLayout(null);
		
		int beginX = 30,beginY = 10,high = 20;
		JLabel imarisCellLabel = new JLabel("imaris cell:");
		imarisCellJudge = new JLabel("false");
		imarisConnect = new Button("connect");
		JLabel voxelLabel = new JLabel("voxel:");
		voxelsizeField = new JTextField(5);
		voxelsizeField.setText(VOXELSIZE+"");
		JLabel physicLabel = new JLabel("phy:");
		physicField = new JTextField(5);
		physicField.setText(PHYSICALRADIUS+"");
		imarisCellLabel.setBounds(beginX, beginY, 90, high);
		imarisCellJudge.setBounds(beginX+=95, beginY, 30, high);
		imarisConnect.setBounds(beginX+=40, beginY, 50, high);
		voxelLabel.setBounds(beginX+=60, beginY, 40, high);
		voxelsizeField.setBounds(beginX+=50, beginY, 40, high);
		physicLabel.setBounds(beginX+=50, beginY, 40, high);
		physicField.setBounds(beginX+=50, beginY, 40, high);
		frame.add(imarisCellLabel);
		frame.add(imarisCellJudge);
		frame.add(imarisConnect);
		frame.add(voxelLabel);
		frame.add(voxelsizeField);
		frame.add(physicLabel);
		frame.add(physicField);
		
		beginX = 30;beginY+=30;
		JLabel imageLabel = new JLabel("annotation:");
		imageField = new JTextField(100);
		imageChoose = new Button("choose");
		imageLabel.setBounds(beginX, beginY, 80, high);
		imageField.setBounds(beginX+=85, beginY, 200, high);
		imageChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(imageLabel);
		frame.add(imageField);
		frame.add(imageChoose);
		
		beginX = 30;beginY+=30;
		JLabel excelLabel = new JLabel("rgb_name:");
		excelField = new JTextField(100);
		excelChoose = new Button("choose");
		excelLabel.setBounds(beginX, beginY, 80, high);
		excelField.setBounds(beginX+=85, beginY, 200, high);
		excelChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(excelLabel);
		frame.add(excelField);
		frame.add(excelChoose);
		
		beginX = 30;beginY+=30;
		JLabel levelLabel = new JLabel("level:");
		levelField = new JTextField(100);
		levelChoose = new Button("choose");
		levelLabel.setBounds(beginX, beginY, 80, high);
		levelField.setBounds(beginX+=85, beginY, 200, high);
		levelChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(levelLabel);
		frame.add(levelField);
		frame.add(levelChoose);
		
		beginX = 130;beginY+=30;
		ok = new Button("ok");
		stop = new Button("stop");
		export = new Button("export");
		ok.setBounds(beginX, beginY, 60, high);
		stop.setBounds(beginX+=70, beginY, 60, high);
		export.setBounds(beginX+=70, beginY, 60, high);
		frame.add(ok);
		frame.add(stop);
		
		frame.setVisible(true);
		event();
	}
	
	private void event() {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				 frame.setVisible(false);
			}
		});
		
		export.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				
				try {
					String[][] nameStr = ExcelTools.getExcelTools().getData(new File(excelField.getText()), 0);
					
					HSSFWorkbook wb = new HSSFWorkbook();
					HSSFSheet sheet = wb.createSheet("test1");
					sheet.setDefaultColumnWidth((short) 18);
					
					int location = 0;
					for(String[] s:nameStr) {
						
						int temp = Integer.valueOf(s[0]);
						if(!regionSpotsMap.containsKey(temp)) continue;
						
						HSSFRow row2 = sheet.createRow(location++);
						HSSFCell cell0 = row2.createCell(0);
						cell0.setCellValue(s[1]);
						
						HSSFCell cell1 = row2.createCell(1);
						cell1.setCellValue(regionSpotsMap.get(temp).size());
					}
					
					try {
						OutputStream outputStream = new FileOutputStream(URL+"/split_cell_counting.xls");
						wb.write(outputStream);
						outputStream.close();
						wb.close();
						JOptionPane.showMessageDialog(null, "finish!", "form",JOptionPane.PLAIN_MESSAGE);
					} catch (Exception e2) {}
					
				} catch (Exception e2) {
				}
				
			}
		});
		
		excelChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				excelField.setText(URL);
			}
		});
		
		
		imageChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				imageField.setText(URL);
			}
		});
		
		levelChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				levelField.setText(URL);
			}
		});
		
		imarisConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BPImarisLib vImarisLib = new BPImarisLib();
					int imarisId = ImarisTools.GetObjectId(vImarisLib);
					System.out.println(imarisId);
					Imaris.IApplicationPrx application = vImarisLib.GetApplication(imarisId);
					Imaris.ISpotsPrx allSpots = application.GetFactory().ToSpots(application.GetSurpassSelection());
					if(allSpots!=null) {
						imarisCellJudge.setText("true");
						CONNECT = true;
					}else {
						imarisCellJudge.setText("false");
					}
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(null, "check your imaris", "error",JOptionPane.PLAIN_MESSAGE);
				}finally {
					imarisCellJudge.setText("false");
				}
			}
		});
		
		stop.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				STOP = true;
			}
		});
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(CONNECT) 
				new Thread(()->{
					try {
						VOXELSIZE = Float.valueOf(voxelsizeField.getText());
						PHYSICALRADIUS = Float.valueOf(physicField.getText());
						RADIUS = VOXELSIZE*PHYSICALRADIUS;
						String imageUrl = imageField.getText();
						String excelUrl = excelField.getText();
						String levelUrl = levelField.getText();
						
						String[][] excelStr = ExcelTools.getExcelTools().getData(new File(excelUrl), 0);
						String[][] levelStr = ExcelTools.getExcelTools().getData(new File(levelUrl), 2);
						Map<Integer, Integer> colorMap = new HashMap<>();
						Map<Integer, String> int2nameMap = new HashMap<>();
						Map<Integer, Integer> parentMap = new HashMap<>();
						
						for(String[] str:levelStr) {
							if(str[4]==null||str[4].length()==0) {
								parentMap.put(Integer.valueOf(str[1]), -1);
							}else {
								parentMap.put(Integer.valueOf(str[1]), Integer.valueOf(str[4]));
							}
							int2nameMap.put(Integer.valueOf(str[1]), str[3]);
						}
						
						for(String[] str:excelStr) {
							int colorTemp = Integer.valueOf(str[2])+Integer.valueOf(str[3])*256+Integer.valueOf(str[4])*256*256;
							int val = Integer.valueOf(str[0]);
							colorMap.put(val, colorTemp);
							int2nameMap.put(val, str[1]);
						}
						
						ImagePlus image = new ImagePlus(imageUrl);
						ImageStack imageStack = image.getImageStack();
						
						BPImarisLib vImarisLib = new BPImarisLib();
						int imarisId = ImarisTools.GetObjectId(vImarisLib);
						System.out.println(imarisId);
						Imaris.IApplicationPrx application = vImarisLib.GetApplication(imarisId);
						Imaris.ISpotsPrx allSpots = application.GetFactory().ToSpots(application.GetSurpassSelection());
						float[][] spotsLocation = allSpots.GetPositionsXYZ();
						
						System.out.println(spotsLocation.length);
						
						int lenX = image.getWidth(), lenY = image.getHeight(), lenZ = image.getStackSize();
						
						boolean[][][] judge = new boolean[lenZ][lenY][lenX];
						for(float[] s:spotsLocation) {
							int x = (int)Math.floor(s[0])/(int)VOXELSIZE,y = (int)Math.floor(s[1])/(int)VOXELSIZE,z = (int)Math.floor(s[2])/(int)VOXELSIZE;
							judge[z][y][x] = true;
						}
						
						Map<Integer, List<int[]>> regionSpotsMap = new HashMap<>();
						
						for(int z=0;z<lenZ;z++) {
							for(int y=0;y<lenY;y++) {
								for(int x=0;x<lenX;x++) {
									int val = (int)imageStack.getVoxel(x, y, z);
									if(val==0||!judge[z][y][x]) continue;
									if(!regionSpotsMap.containsKey(val)) regionSpotsMap.put(val, new ArrayList<>());
									regionSpotsMap.get(val).add(new int[] {x,y,z});
								}
							}
						}
						
						Imaris.IDataContainerPrx vGroup = 
								  application.GetFactory().CreateDataContainer();
						vGroup.SetName("cell");
						Imaris.IDataContainerPrx scence =  application.GetSurpassScene();
						scence.AddChild(vGroup, -1);
						
						Map<Integer, Imaris.IDataContainerPrx> groupMap = new HashMap<>();
						Imaris.IDataContainerPrx nilGroup = 
								  application.GetFactory().CreateDataContainer();
						nilGroup.SetName("nil");
						vGroup.AddChild(nilGroup, -1);
						for(Map.Entry<Integer, List<int[]>> m:regionSpotsMap.entrySet()) {
							if(STOP) {STOP = false;return;}
							int val = m.getKey();
							List<int[]> locationTemp = m.getValue();
							float[][] temp = new float[locationTemp.size()][3];
							for(int i=0;i<locationTemp.size();i++) {
								temp[i][0] = locationTemp.get(i)[0]*VOXELSIZE;
								temp[i][1] = locationTemp.get(i)[1]*VOXELSIZE;
								temp[i][2] = locationTemp.get(i)[2]*VOXELSIZE;
							}
							Imaris.ISpotsPrx newSpots = application.GetFactory().CreateSpots();
							int[] arg1 = new int[temp.length];
							float[] arg2 = new float[temp.length];
							for(int i=0;i<temp.length;i++) {
								arg2[i] = RADIUS;
							}
							newSpots.Set(temp, arg1, arg2);
							newSpots.SetName(int2nameMap.get(val));
							newSpots.begin_SetColorRGBA(colorMap.get(val));
							if(!parentMap.containsKey(val)) {
								nilGroup.AddChild(newSpots, -1);
							}else {
								int parentId = parentMap.get(val);
								if(!groupMap.containsKey(parentId)) {
									Imaris.IDataContainerPrx tempGroup = 
											  application.GetFactory().CreateDataContainer();
									tempGroup.SetName(int2nameMap.get(parentId));
									groupMap.put(parentId, tempGroup);
								}
								groupMap.get(parentId).AddChild(newSpots, -1);
								while(parentId!=-1) {
									int tempId = parentId;
									parentId = parentMap.get(parentId);
									if(!groupMap.containsKey(parentId)) {
										Imaris.IDataContainerPrx tempGroup = 
												  application.GetFactory().CreateDataContainer();
										tempGroup.SetName(int2nameMap.get(parentId));
										groupMap.put(parentId, tempGroup);
									}
									groupMap.get(parentId).AddChild(groupMap.get(tempId), -1);
								}
							}
						}
						vGroup.AddChild(groupMap.get(-1), -1);
					} catch (Exception e2) {
					}
				}).start(); 
				else
					JOptionPane.showMessageDialog(null, "have not connect", "error",JOptionPane.PLAIN_MESSAGE);
				
			}
		});
	}
	
	public static void main(String[] args) throws Exception{
		new SplitCountingToImaris().location();
	}

	@Override
	public void run(String arg) {
		location();
	}

}
