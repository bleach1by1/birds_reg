package I.precise.main;

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import I.coarse.entity.LineEntity;
import I.coarse.tools.ChangeParamTools;
import I.entity.MaskPointEntity;
import I.plugin.GlobalValue;
import I.plugin.LogIn;
import I.precise.tools.LineTools;
import I.utils.ArrowTools;
import I.utils.FileCopyUtils;
import I.utils.RegistarionTools;
import I.utils.ExcelTools;
import I.utils.WaterMarkTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.RGBStackMerge;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * precise registration module
 * @author zengweilin
 *
 */

public class PreciseRegistration implements MouseListener,MouseMotionListener{

	public ImagePlus fuseImage = null;
	public static ImagePlus lineImage = null;
	public static ImagePlus annotationImage = null;
	public Map<Integer, String> grayMap = null;
	public MouseListener mouseListener;
	
	
	private RGBStackMerge rgbStackMerge;
	private Map<Integer, Map<Integer, List<MaskPointEntity>>> maskPointMap;
	private Map<Integer, Map<Integer, LineEntity>> pointMap;
	private Map<Integer, Integer> arrowNumberMap;
	private LineEntity line;
	private int[] stringBlockSize;
	
	private static PreciseRegistration preciseregistration = null;
	
	public static PreciseRegistration getPreciseregistration() {
		if(preciseregistration==null) {
			synchronized (PreciseRegistration.class) {
				if(preciseregistration==null) {
					preciseregistration = new PreciseRegistration();
				}
			}
		}
		return preciseregistration;
	}
	
	public void addMouseListener() {
		try {
			if(!new File(GlobalValue.URL+"/cache/rgb_name.xls").exists())
				FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("rgb_name.xls"),GlobalValue.URL+"/cache/rgb_name.xls");
			String[][] grayMapStr = ExcelTools.getExcelTools().
					getData(new File(GlobalValue.URL+"/cache/rgb_name.xls"), 0);
			//new File(GlobalValue.URL+"/cache/rgb_name.xls").delete();
			PreciseRegistration.getPreciseregistration().grayMap = new HashMap<>();
			for(String[] g:grayMapStr){
				PreciseRegistration.getPreciseregistration().grayMap.put(Integer.valueOf(g[0]), g[1]);
			}
		} catch (Exception e) {
		}
		fuseImage.show();
		fuseImage.getCanvas().addMouseListener(this);
		fuseImage.getCanvas().addMouseMotionListener(this);
	}
	
	private PreciseRegistration() {}
	
	private JTextField gridX,gridY,iter;
	private Button deleteButton,backButton;
	private Button deformButton,saveButton;
	private JCheckBox useBeforeBox;
	private JLabel xLabel,yLabel,classNameLabel;
	
	
	public JPanel location() {
		JPanel outPanel = new JPanel();
		outPanel.setLayout(null);
		
		int beginX = 30,beginY = 30,high = 20;
		JLabel parameterLabel = new JLabel("parameter:");
		JLabel gridXLabel = new JLabel("gridX:");
		gridX = new JTextField(4);
		JLabel gridYLabel = new JLabel("gridY:");
		gridY = new JTextField(4);
		JLabel iterLabel = new JLabel("iter:");
		iter = new JTextField(4);
		parameterLabel.setBounds(beginX, beginY, 90, high);
		gridXLabel.setBounds(beginX+=100, beginY, 40, high);
		gridX.setBounds(beginX+=40, beginY, 30, high);
		gridYLabel.setBounds(beginX+=40, beginY, 40, high);
		gridY.setBounds(beginX+=40, beginY, 30, high);
		iterLabel.setBounds(beginX+=40, beginY, 40, high);
		iter.setBounds(beginX+=40, beginY, 40, high);
		gridX.setText(30.0+"");
		gridY.setText(30.0+"");
		iter.setText(10+"");
		outPanel.add(parameterLabel);
		outPanel.add(gridXLabel);
		outPanel.add(gridX);
		outPanel.add(gridYLabel);
		outPanel.add(gridY);
		outPanel.add(iterLabel);
		outPanel.add(iter);
		
		beginX = 30;beginY +=50;
		JLabel toolsLabel = new JLabel("deformation:");
		deleteButton = new Button("delete");
		backButton = new Button("back");
		deformButton = new Button("deform");
		useBeforeBox = new JCheckBox("use before", false);
		saveButton = new Button("save");
		toolsLabel.setBounds(beginX, beginY, 90, high);
		deleteButton.setBounds(beginX+=100, beginY, 40, high);
		backButton.setBounds(beginX+=60, beginY, 40, high);
		useBeforeBox.setBounds(beginX+=60, beginY, 90, high);
		deformButton.setBounds(beginX+=90, beginY, 45, high);
		saveButton.setBounds(beginX+=60, beginY, 40, high);
		outPanel.add(toolsLabel);
		outPanel.add(deleteButton);
		outPanel.add(backButton);
		outPanel.add(deformButton);
		outPanel.add(useBeforeBox);
		outPanel.add(saveButton);
		
		beginX = 30;beginY+=50;high = 35;
		Font font = new Font("", Font.ROMAN_BASELINE, 22);
		
		JLabel locationLabel = new JLabel("location:");
		JLabel xJLabel = new JLabel("x:");
		xLabel = new JLabel("nil");
		JLabel yJLabel = new JLabel("y:");
		yLabel = new JLabel("nil");
		JLabel nameLabel = new JLabel("name:");
		classNameLabel = new JLabel("nil");
		locationLabel.setBounds(beginX, beginY, 50, high);
		xJLabel.setBounds(beginX+=70, beginY, 60, high);
		xLabel.setBounds(beginX+=30, beginY, 50, high);
		yJLabel.setBounds(beginX+=60, beginY, 60, high);
		yLabel.setBounds(beginX+=30, beginY, 50, high);
		nameLabel.setBounds(beginX+=50, beginY, 70, high);
		classNameLabel.setBounds(beginX+=70, beginY, 160, high);
		xJLabel.setFont(font);
		xLabel.setFont(font);
		yJLabel.setFont(font);
		yLabel.setFont(font);
		nameLabel.setFont(font);
		classNameLabel.setFont(font);
		outPanel.add(locationLabel);
		outPanel.add(xJLabel);
		outPanel.add(xLabel);
		outPanel.add(yJLabel);
		outPanel.add(yLabel);
		outPanel.add(nameLabel);
		outPanel.add(classNameLabel);
		
		mouseListener = this;
		maskPointMap = new HashMap<>();
		arrowNumberMap = new HashMap<>();
		pointMap = new HashMap<>();
		stringBlockSize = new int[2];
		rgbStackMerge = new RGBStackMerge();
		event();
		return outPanel;
		
	}
	
	private void event() {
		
		deleteButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				int current = fuseImage.getCurrentSlice(),lenX = GlobalValue.downSampleImage.getWidth(), lenY = GlobalValue.downSampleImage.getHeight();
				
				pointMap.put(current, new HashMap<>());
				arrowNumberMap.put(current, 1);
				ImagePlus orginalImagePlus = NewImage.createByteImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
				ImageStack orginalImageStack = orginalImagePlus.getImageStack();
				ImagePlus lineImagePlus = NewImage.createByteImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
				ImageStack lineImageStack = lineImagePlus.getImageStack();
				orginalImageStack.setProcessor(GlobalValue.downSampleImage.getImageStack().getProcessor(current), 1);
				lineImageStack.setProcessor(lineImage.getImageStack().getProcessor(current), 1);
				
				ImageStack fuseCoronalOneImageStack = rgbStackMerge.mergeStacks(lenX, lenY, 1,
						orginalImageStack, lineImageStack, null, true);
				
				ImageStack fuseCoronalImageStack = fuseImage.getImageStack();
				fuseCoronalImageStack.setProcessor(fuseCoronalOneImageStack.getProcessor(1), current);
				fuseImage.setStack(fuseCoronalImageStack);
				fuseImage.show();
				
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					new FileSaver(annotationImage).saveAsTiff(GlobalValue.URL+"/registration/precise/result.tif");
				} catch (Exception e2) {
					LogIn.error(e2.getMessage());
				}
			}
		});
		
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int current = fuseImage.getCurrentSlice(),lenX = GlobalValue.downSampleImage.getWidth(), lenY = GlobalValue.downSampleImage.getHeight();
				String root = GlobalValue.URL;
				String rootAdd = root+"/registration/precise";
				ImagePlus transormAnn = new ImagePlus(rootAdd+"/move.tif");
				
				ImageStack lineStack = lineImage.getImageStack();
				ImageStack annotationImageStack = annotationImage.getImageStack();
				
				ImageProcessor lineProcessor = lineStack.getProcessor(current);
				ImageProcessor annotationProcessor = transormAnn.getProcessor();
				
				for(int x=0;x<lenX;x++) {
					for(int y=0;y<lenY;y++) {
						lineProcessor.putPixelValue(x, y, 0);
					}
				}
				
				lineProcessor = LineTools.getLineTool().lineMinGet(annotationProcessor, lineProcessor);
				lineStack.setProcessor(lineProcessor, current);
				annotationImageStack.setProcessor(annotationProcessor, current);
				
				ImagePlus orginalImagePlus = NewImage.createByteImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
				ImageStack orginalImageStack = orginalImagePlus.getImageStack();
				ImagePlus lineImagePlus = NewImage.createByteImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
				ImageStack lineImageStack = lineImagePlus.getImageStack();
				orginalImageStack.setProcessor(GlobalValue.downSampleImage.getImageStack().getProcessor(current), 1);
				lineImageStack.setProcessor(lineProcessor, 1);
				
				ImageStack fuseCoronalOneImageStack = rgbStackMerge.mergeStacks(lenX, lenY, 1,
						orginalImageStack, lineImageStack, null, true);
				
				ImageStack fuseCoronalImageStack = fuseImage.getImageStack();
				fuseCoronalImageStack.setProcessor(fuseCoronalOneImageStack.getProcessor(1), current);
				fuseImage.setStack(fuseCoronalImageStack);
				fuseImage.show();
			}
		});
		
		deformButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int current = fuseImage.getCurrentSlice(),lenX = GlobalValue.downSampleImage.getWidth(), lenY = GlobalValue.downSampleImage.getHeight();
				String root = GlobalValue.URL;
				String rootAdd = root+"/registration/precise";
				new File(rootAdd+"/out").mkdirs();
				
				ImagePlus fixImage = NewImage.createByteImage("fix", lenX, lenY, 1, NewImage.FILL_BLACK);
				fixImage.setProcessor(GlobalValue.downSampleImage.getImageStack().getProcessor(current));
				FileSaver fileSaver = new FileSaver(fixImage);
				fileSaver.saveAsTiff(rootAdd+"/fix.tif");
				
				ImagePlus moveImage = NewImage.createShortImage("move", lenX, lenY, 1, NewImage.FILL_BLACK);
				moveImage.setProcessor(annotationImage.getImageStack().getProcessor(current));
				FileSaver moveSaver = new FileSaver(moveImage);
				moveSaver.saveAsTiff(rootAdd+"/move.tif");
				
				try {
					
					FileWriter saveMove = new FileWriter(rootAdd+"/movePoint.txt");
					BufferedWriter saveMoveWriter = new BufferedWriter(saveMove);
					saveMoveWriter.write("point\n");
					saveMoveWriter.write(pointMap.get(current).size()+"\n\n");
					
					FileWriter saveFix = new FileWriter(rootAdd+"/fixPoint.txt");
					BufferedWriter saveFixWriter = new BufferedWriter(saveFix);  
					saveFixWriter.write("point\n");
					saveFixWriter.write(pointMap.get(current).size()+"\n\n");
					
					for(Map.Entry<Integer, LineEntity> m:pointMap.get(current).entrySet()){
						saveFixWriter.write(m.getValue().getEnd().x+" "+m.getValue().getEnd().y+"\n");
						saveMoveWriter.write(m.getValue().getBegin().x+" "+m.getValue().getBegin().y+"\n");
					}
					pointMap.put(current, new HashMap<>());
					saveMoveWriter.close();
					saveMove.close();
					
					saveFixWriter.close();
					saveFix.close();
					
				} catch (Exception e2) {}
				
				if(!new File(GlobalValue.URL+"/cache/para-Standard3D-withPoints.txt").exists())
					FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/para-Standard3D-withPoints.txt"), GlobalValue.URL+"/cache/para-Standard3D-withPoints.txt");
				if(!new File(GlobalValue.URL+"/cache/elastix.exe").exists())
					FileCopyUtils.fileCopy(this.getClass().getResourceAsStream("/elastix.exe"), GlobalValue.URL+"/cache/elastix.exe");
				if(!new File(GlobalValue.URL+"/cache/ANNlib-4.9.dll").exists())
					FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("ANNlib-4.9.dll"), GlobalValue.URL+"/cache/ANNlib-4.9.dll");
				
				String cmd = GlobalValue.URL+"/cache/elastix.exe";
				cmd+=" -f "+rootAdd+"/fix.tif";
				cmd+=" -m "+rootAdd+"/move.tif";
				cmd+=" -fp "+rootAdd+"/fixPoint.txt";
				cmd+=" -mp "+rootAdd+"/movePoint.txt";
				cmd+=" -p "+GlobalValue.URL+"/cache/para-Standard3D-withPoints.txt";
				cmd+=" -out "+rootAdd+"/out";
				
				Map<String, String> changeMap = new HashMap<>();
				changeMap.put("FinalGridSpacingInPhysicalUnits", gridX.getText().trim()+" "+gridY.getText().trim());
				changeMap.put("MaximumNumberOfIterations", iter.getText().trim());
				ChangeParamTools.changeParam(this.getClass().getResource("/para-Standard3D-withPoints.txt").getPath(), changeMap);
				
				cmd+=" -p "+this.getClass().getResource("/para-Standard3D-withPoints.txt").getPath();
				
				RegistarionTools.shell(cmd);
				
				ImagePlus transormAnn = new ImagePlus(rootAdd+"/out/result.0.tif");
				
				ImageStack lineStack = lineImage.getImageStack();
				ImageStack annotationImageStack = annotationImage.getImageStack();
				
				ImageProcessor lineProcessor = lineStack.getProcessor(current);
				ImageProcessor annotationProcessor = transormAnn.getProcessor();
				
				for(int x=0;x<lenX;x++) {
					for(int y=0;y<lenY;y++) {
						lineProcessor.putPixelValue(x, y, 0);
					}
				}
				
				lineProcessor = LineTools.getLineTool().lineMinGet(annotationProcessor, lineProcessor);
				lineStack.setProcessor(lineProcessor, current);
				annotationImageStack.setProcessor(annotationProcessor, current);
				
				ImagePlus orginalImagePlus = NewImage.createByteImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
				ImageStack orginalImageStack = orginalImagePlus.getImageStack();
				ImagePlus lineImagePlus = NewImage.createByteImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
				ImageStack lineImageStack = lineImagePlus.getImageStack();
				orginalImageStack.setProcessor(GlobalValue.downSampleImage.getImageStack().getProcessor(current), 1);
				lineImageStack.setProcessor(lineProcessor, 1);
				
				ImageStack fuseOneImageStack = rgbStackMerge.mergeStacks(lenX, lenY, 1,
						orginalImageStack, lineImageStack, null, true);
				
				ImageStack fuseImageStack = fuseImage.getImageStack();
				fuseImageStack.setProcessor(fuseOneImageStack.getProcessor(1), current);
				fuseImage.setStack(fuseImageStack);
				fuseImage.show();
				
			}
		});
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setSize(500, 300);
		f.add(new PreciseRegistration().location());
		f.setVisible(true);
	}

	public void mouseMoved(MouseEvent e) {
		int currentSlice = fuseImage.getCurrentSlice(); 
		Point tempPoint = fuseImage.getCanvas().getCursorLoc();
		xLabel.setText(tempPoint.x+"");
		yLabel.setText(tempPoint.y+"");
		int val = (int)(annotationImage.getImageStack().getVoxel(tempPoint.x, tempPoint.y, currentSlice-1));
		if(val==0) classNameLabel.setText("nil");
		else classNameLabel.setText(grayMap.get(val));
		
	}
	public void mouseDragged(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		xLabel.setText("nil");
		yLabel.setText("nil");
		classNameLabel.setText("nil");
	}
	public void mousePressed(MouseEvent e) {
		line = new LineEntity();
		line.setBegin(fuseImage.getCanvas().getCursorLoc());
	}

	public void mouseReleased(MouseEvent e) {
		int currentSlice = fuseImage.getCurrentSlice(); 
		line.setEnd(fuseImage.getCanvas().getCursorLoc());
		if(!pointMap.containsKey(currentSlice)) pointMap.put(currentSlice, new HashMap<>());
		if(!arrowNumberMap.containsKey(currentSlice)) arrowNumberMap.put(currentSlice, 1);
		if(!maskPointMap.containsKey(currentSlice)) maskPointMap.put(currentSlice, new HashMap<>());
		pointMap.get(currentSlice).put(arrowNumberMap.get(currentSlice), line);
		
		ImageStack fuseSagittalImageStack = fuseImage.getImageStack();
		ColorProcessor colorProcessor = fuseSagittalImageStack.getProcessor(currentSlice).convertToColorProcessor();
		List<MaskPointEntity> linePointList = ArrowTools.drawLinePoint(line.getBegin().x, line.getBegin().y, line.getEnd().x, line.getEnd().y,
				fuseImage);
		String waterMark = String.valueOf(arrowNumberMap.get(currentSlice));
		stringBlockSize[0] = waterMark.length()*10;
		stringBlockSize[1] = 16;
		Color color = new Color(255, 255, 255, 180);
		Font font = new Font("", Font.PLAIN, 7);
		colorProcessor = WaterMarkTools.addWaterMark(colorProcessor, line.getEnd().x, line.getEnd().y, waterMark,color,font);
		colorProcessor = ArrowTools.drawLine(line.getBegin().x, line.getBegin().y, line.getEnd().x, line.getEnd().y,
				colorProcessor, 1);
		for(int x=line.getEnd().x;x<stringBlockSize[0]+line.getEnd().x;x++) {
			for(int y=line.getEnd().y-stringBlockSize[1];y<line.getEnd().y;y++) {
				MaskPointEntity maskPoint = new MaskPointEntity();
				maskPoint.x = x; maskPoint.y = y;
				int[] temp = fuseImage.getPixel(x, y);
				maskPoint.r = temp[0];
				maskPoint.g = temp[1];
				maskPoint.b = temp[2];
				linePointList.add(maskPoint);
			}
		}
		
		maskPointMap.get(currentSlice).put(arrowNumberMap.get(currentSlice), linePointList);
		fuseSagittalImageStack.setProcessor(colorProcessor, currentSlice);
		fuseImage.setStack(fuseSagittalImageStack);
		fuseImage.deleteRoi();
		fuseImage.show();
		arrowNumberMap.put(currentSlice, arrowNumberMap.get(currentSlice)+1);
	}

}
