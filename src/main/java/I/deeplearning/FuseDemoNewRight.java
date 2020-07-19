package I.deeplearning;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import I.utils.ExcelTools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.PlugIn;

public class FuseDemoNewRight implements PlugIn{

	private static boolean JUDGE = false;
	
	public JFrame frame;
	public JTextField regionUrl,excelUrl,outUrl,levelUrl,nameUrl;
	public Button regionChoose,excelChoose,outUrlChoose,levelChoose,nameChoose,ok;
	
	public void location() {
		frame = new JFrame("genrate training data");
		frame.setBounds(300, 400, 450, 250);
		frame.setLayout(null);
		
		int beginX = 30,beginY = 10,high = 20;
		JLabel regionLabel = new JLabel("ann url:");
		regionUrl = new JTextField(100);
		regionChoose = new Button("choose");
		regionLabel.setBounds(beginX, beginY, 80, high);
		regionUrl.setBounds(beginX+=85, beginY, 200, high);
		regionChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(regionLabel);
		frame.add(regionUrl);
		frame.add(regionChoose);
		
		beginX = 30; beginY += 30;
		JLabel excelLabel = new JLabel("excel url:");
		excelUrl = new JTextField(100);
		excelChoose = new Button("choose");
		excelLabel.setBounds(beginX, beginY, 80, high);
		excelUrl.setBounds(beginX+=85, beginY, 200, high);
		excelChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(excelLabel);
		frame.add(excelUrl);
		frame.add(excelChoose);
		
		beginX = 30; beginY += 30;
		JLabel levelLabel = new JLabel("level url:");
		levelUrl = new JTextField(100);
		levelChoose = new Button("choose");
		levelLabel.setBounds(beginX, beginY, 80, high);
		levelUrl.setBounds(beginX+=85, beginY, 200, high);
		levelChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(levelLabel);
		frame.add(levelUrl);
		frame.add(levelChoose);
		
		beginX = 30; beginY += 30;
		JLabel nameLabel = new JLabel("name url:");
		nameUrl = new JTextField(100);
		nameChoose = new Button("choose");
		nameLabel.setBounds(beginX, beginY, 80, high);
		nameUrl.setBounds(beginX+=85, beginY, 200, high);
		nameChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(nameLabel);
		frame.add(nameUrl);
		frame.add(nameChoose);
		
		beginX = 30; beginY += 30;
		JLabel outUrlLabel = new JLabel("out url:");
		outUrl = new JTextField(100);
		outUrlChoose = new Button("choose");
		outUrlLabel.setBounds(beginX, beginY, 80, high);
		outUrl.setBounds(beginX+=85, beginY, 200, high);
		outUrlChoose.setBounds(beginX+=210, beginY, 60, high);
		frame.add(outUrlLabel);
		frame.add(outUrl);
		frame.add(outUrlChoose);
		
		beginX = 180; beginY += 30;
		ok = new Button("ok");
		ok.setBounds(beginX, beginY, 40, high);
		frame.add(ok);
		
		event();
		frame.setVisible(true);
	}
	
	private void event() {
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
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				regionUrl.setText(URL);
			}
		});
		
		excelChoose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				excelUrl.setText(URL);
			}
		});
		
		nameChoose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				nameUrl.setText(URL);
			}
		});
		
		levelChoose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				levelUrl.setText(URL);
			}
		});
		
		ok.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(!JUDGE) {
					new Thread(()->{
						JUDGE = true;
						try {
							IJ.log("begin fuse");
							String levelStr = levelUrl.getText(),nameStr = nameUrl.getText(), annStr = regionUrl.getText(),excelStr = excelUrl.getText(),outStr = outUrl.getText();
							ImagePlus ann = new ImagePlus(annStr);
							String annTitle = ann.getTitle();
							try {
								int bitDepth = ExcelTools.getExcelTools().getData(new File(excelStr), 1).length>=255?16:8;
								ann = fuseDemo(levelStr, nameStr, excelStr, ann, bitDepth);
								new FileSaver(ann).saveAsTiff(outStr+"/"+annTitle);
							} catch (Exception e2) {
								IJ.log(e2.getMessage());
							}
							IJ.log("end fuse");
						} catch (Exception e2) {
							IJ.log(e2.getMessage());
						}finally {
							JUDGE = false;
						}
						JUDGE = false;
					}) .start();
				}else {
					JOptionPane.showMessageDialog(null, "this is running", "error",JOptionPane.PLAIN_MESSAGE);
				}
				
			}
		});
	}
	
	public static ImagePlus fuseDemo(String chooseLevelUrl,String chooseNameUrl,String inUrl, ImagePlus image, int bitDepth) {
		ImageStack imageStack = image.getImageStack();
		int lenX = image.getWidth(), lenY = image.getHeight(), lenZ = image.getStackSize();
		ImagePlus out = NewImage.createImage("out", lenX, lenY, lenZ, bitDepth, NewImage.FILL_BLACK);

		try {
			String[][] excelLevelStr = ExcelTools.getExcelTools().getData(new File(chooseLevelUrl), 2);
			String[][] excelRegionStr = ExcelTools.getExcelTools().getData(new File(chooseNameUrl), 1);
			String[][] inStr = ExcelTools.getExcelTools().getData(new File(inUrl), 0);
			ImageStack outStack = out.getImageStack();
			Map<Integer, Integer> valMap = new HashMap<>();
			Set<Integer> regionSet = new HashSet<>();
			Map<String, Integer> regionMap = new HashMap<>();
			Map<Integer, Integer> depthMap = new HashMap<>();
			
			for(int i=1;i<=inStr.length;i++) {
				regionMap.put(inStr[i-1][0], i);
			}
			
			for(String[] str:excelRegionStr) {
				int val = Integer.valueOf(str[0]);
				if(regionMap.containsKey(str[1])) {
					valMap.put(val, regionMap.get(str[1]));
				}
				regionSet.add(val);
			}
			
			Map<Integer, Integer> levelValMap = new HashMap<>();
			for(String[] str:excelLevelStr) {
				if(regionMap.containsKey(str[3])) {
					levelValMap.put(Integer.valueOf(str[1]), regionMap.get(str[3]));
					depthMap.put(Integer.valueOf(str[1]), Integer.valueOf(str[5]));
				}
			}
			
			for(String[] str:excelLevelStr) {
				for(Map.Entry<Integer, Integer> m:levelValMap.entrySet()) {
					int depth = depthMap.get(m.getKey()),depthTemp = Integer.valueOf(str[5]);
					if(depthTemp<depth) continue;
					int judgeChoose = Integer.valueOf(str[6].substring(1, str[6].length()-1).split("/")[depth]); 
					if(judgeChoose==m.getKey()) {
						int val = Integer.valueOf(str[1]);
						if(regionSet.contains(val)) {
							valMap.put(val, m.getValue());
						}
						break;
					}
				}
			}
			
			for(int z=0;z<lenZ;z++) {
				for(int y=0;y<lenY;y++) {
					for(int x=0;x<lenX;x++) {
						int val = (int)imageStack.getVoxel(x, y, z);
						if(valMap.containsKey(val)) {
							outStack.setVoxel(x, y, z, valMap.get(val));
						}
					}
				}
			}
			out.setStack(outStack);
		} catch (Exception e) {
			IJ.log(e.getMessage());
		}
		return out;
	}
	
	public static void main(String[] args) throws Exception{
		FuseDemoNewRight f = new FuseDemoNewRight();
		f.run(null);
		f.regionUrl.setText("I:\\birds\\cache\\annotationOrgImage.tif");
		f.excelUrl.setText("I:\\birds\\cache\\rgb_name.xls");
		f.levelUrl.setText("I:\\birds\\cache\\level.xls");
		f.nameUrl.setText("I:\\han\\dl\\region_19_judge.xls");
		f.outUrl.setText("I:\\");
	}

	@Override
	public void run(String arg) {
		location();
	}

}
 