package I.utils;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.plugin.WandToolOptions;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LineRegionUtils implements PlugIn,MouseListener{

	
	private JFrame frame;
	private JTextField classId;
	private JTextField imageName;
	
	private Button choose;
	private Button ok;
	private Button delete;
	private Button export;
	
	private ImagePlus line;
	private ImagePlus lineRGB;
	
	private ImageStack lineStack;
	private ImageStack lineRGBStack;
	
	
	private Map<String, Map<Integer, List<Point[]>>> tempRegionMap;
	private Map<String, ImagePlus> regionImageMap;
	
	private MouseListener mouseListener;
	
	int lenX,lenY,lenZ;
	
	public void location(){
		frame = new JFrame();
		
		frame.setBounds(300, 300, 400, 150);
		frame.setLayout(new GridLayout(2,1,1,1));
		
		JPanel imagePanel = new JPanel();
		JLabel imageLabel = new JLabel("image:");
		imageName = new JTextField(20);
		choose = new Button("choose");
		imagePanel.add(imageLabel);
		imagePanel.add(imageName);
		imagePanel.add(choose);
		
		JPanel classPanel = new JPanel();
		JLabel className = new JLabel("className:");
		classId = new JTextField(5);
		ok = new Button("ok");
		delete = new Button("delete");
		export = new Button("export");
		classPanel.add(className);
		classPanel.add(classId);
		classPanel.add(ok);
		classPanel.add(delete);
		classPanel.add(export);
		
		frame.add(imagePanel);
		frame.add(classPanel);
		frame.setVisible(true);
		event();
	}
	
	private void event(){
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				frame.setVisible(false);
			}
		});
		
		choose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				line = IJ.getImage();
				lineStack = line.getImageStack();
				lenX = line.getWidth();lenY = line.getHeight();
				lenZ = line.getStackSize();
				lineRGB = NewImage.createRGBImage("lineRGB", lenX, lenY, lenZ, NewImage.FILL_BLACK);
				lineRGBStack = lineRGB.getImageStack();
				for(int z=1;z<=lenZ;z++){
					lineRGBStack.setProcessor(lineStack.getProcessor(z).convertToRGB(), z);
				}
				lineRGB.show();
				tempRegionMap = new HashMap<>();
				regionImageMap = new HashMap<>();
				System.out.println(lenX+" "+lenY+" "+lenZ);
				imageName.setText(line.getShortTitle());
				lineRGB.getCanvas().addMouseListener(mouseListener);
			}
		});
		
		ok.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int current = lineRGB.getCurrentSlice();
				String classStr = classId.getText();
				if(!regionImageMap.containsKey(classStr)){
					regionImageMap.put(classStr, NewImage.createByteImage
						("", lenX, lenY, lenZ, NewImage.FILL_BLACK));
				}
				ImageStack regionImageStack = regionImageMap.get(classStr).getImageStack();
				
				for(Map.Entry<Integer, List<Point[]>> m:tempRegionMap.get(classStr).entrySet()) {
					for(Point[] pArr:m.getValue()) {
						for(Point p:pArr) {
							lineStack.setVoxel(p.x, p.y, m.getKey()-1, 255);
							regionImageStack.setVoxel(p.x, p.y, m.getKey()-1, 255);
							lineRGBStack.setVoxel(p.x, p.y, m.getKey()-1, 255);
						}
					}
				}
				
				tempRegionMap.remove(classStr);
				regionImageMap.get(classStr).setStack(regionImageStack);
				line.setStack(lineStack);
				lineRGB.setStack(lineRGBStack);
				lineRGB.show();

			}
		});
		
		export.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try{
					JFileChooser jfc=new JFileChooser();
					jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
					jfc.showDialog(new JLabel(), "choose");
					File file=jfc.getSelectedFile();
					if(file.isFile()) return;
					for(Map.Entry<String, ImagePlus> i: regionImageMap.entrySet()){
						new File(file.getPath()+"\\out\\"+i.getKey()+"_gd").mkdirs();
						ImageStack tempStack = i.getValue().getImageStack();
						for(int z=0;z<lenZ;z++){
							ImagePlus saveImage = NewImage.createRGBImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
							ImageStack saveImageStack = saveImage.getImageStack();
							saveImageStack.setProcessor(tempStack.getProcessor(z+1).convertToRGB(),1);
							saveImage.setStack(saveImageStack);
							FileSaver saver = new FileSaver(saveImage);
							saver.saveAsPng(file.getPath()+"\\out\\"+i.getKey()+"_gd"+"\\"+z+".png");
						}
					}
				}catch(Exception e2){
					System.out.println(e2.getMessage());
					return;
				}
			}
		});
	}
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		new ImageJ();
		new LineRegionUtils().run(null);
	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		location();
		mouseListener = this;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		Point location = lineRGB.getCanvas().getCursorLoc();
		int current = lineRGB.getCurrentSlice();
		
		Wand w = new Wand(lineRGBStack.getProcessor(current));
		
        w.autoOutline(location.x, location.y);
		WandToolOptions.setStart(location.x, location.y);
		
		Polygon poly = new Polygon(w.xpoints, w.ypoints, w.npoints);
		PolygonRoi polygonRoi = new PolygonRoi(poly, Roi.TRACED_ROI);
		Point[] reflinePoints = polygonRoi.getContainedPoints();
		
		for(Point p:reflinePoints){
			lineRGBStack.setVoxel(p.x, p.y, current-1, 250);
		}
		if(!tempRegionMap.containsKey(classId.getText())) tempRegionMap.put(classId.getText(), new HashMap<>());
		if(!tempRegionMap.get(classId.getText()).containsKey(current)) tempRegionMap.get(classId.getText()).put(current, new ArrayList<>());
		tempRegionMap.get(classId.getText()).get(current).add(reflinePoints);
		lineRGB.setStack(lineRGBStack);
		lineRGB.show();
	}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}
