package I.deeplearning;

import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import I.downsample.main.DownSample;
import I.plugin.GlobalValue;
import I.plugin.LogIn;
import I.utils.ExcelTools;
import I.utils.FileCopyUtils;
import I.utils.RegistarionTools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.RGBStackMerge;

/**
 * Segnet module 
 * @author zengweilin
 *
 */

public class Segnet implements MouseMotionListener,MouseListener {

	private JTextField log, imageUrl, upload;
	private Button chooseLog, chooseImage, chooseUpload, validation;
	private JLabel xLabel,yLabel,classNameLabel;
	public MouseListener mouseListener;
	public MouseMotionListener mouseMotionListener;
	
	private Map<Integer, String> grayMap;
	private static Segnet segnet = null;
	
	private ImagePlus lineImage,tempImage,annotationImage,fuseImage;
	
	/**needn't to construct this method*/
	private Segnet() {}
	
	public static void main(String[] args) throws IOException{
		Segnet d = new Segnet();
		Frame f = new Frame();
		f.setSize(500, 200);
		f.add(d.location());
		f.setVisible(true);
	}
	
	public final static Segnet getSegnet() {
		if(segnet==null) {
			synchronized (DownSample.class) {
				if(segnet==null) {
					segnet = new Segnet();
				}
			}
		}
		return segnet;
	}
	
	public JPanel location(){
		JPanel outPanel = new JPanel();
		outPanel.setLayout(null);
		int beginX = 40,beginY = 10,high = 25;
		JLabel logLabel = new JLabel("log:");
		log = new JTextField(20);
		chooseLog = new Button("choose");
		logLabel.setBounds(beginX, beginY, 60, high);
		log.setBounds(beginX+=60, beginY, 200, high);
		chooseLog.setBounds(beginX+=210, beginY, 50, high);
		outPanel.add(logLabel);
		outPanel.add(log);
		outPanel.add(chooseLog);
		
		beginX = 40;beginY += 30;
		JLabel imageLabel = new JLabel("image:");
		imageUrl = new JTextField(20);
		chooseImage = new Button("choose");
		imageLabel.setBounds(beginX, beginY, 60, high);
		imageUrl.setBounds(beginX+=60, beginY, 200, high);
		chooseImage.setBounds(beginX+=210, beginY, 50, high);
		outPanel.add(imageLabel);
		outPanel.add(imageUrl);
		outPanel.add(chooseImage);
		
		beginX = 40; beginY += 30;
		JLabel uploadLabel = new JLabel("parame:");
		upload = new JTextField(20);
		chooseUpload = new Button("upload");
		uploadLabel.setBounds(beginX, beginY, 50, high);
		upload.setBounds(beginX+=60, beginY, 200, high);
		chooseUpload.setBounds(beginX+=210, beginY, 50, high);
		outPanel.add(uploadLabel);
		outPanel.add(upload);
		outPanel.add(chooseUpload);
		
		beginX = 170;beginY += 30;
		validation = new Button("validation");
		validation.setBounds(beginX, beginY, 70, high);
		outPanel.add(validation);
		
		beginX = 30;beginY+=30;high = 35;
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
		event();
		mouseListener = this;
		mouseMotionListener = this;
		return outPanel;
	}
	
	private void event(){
		chooseLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				log.setText(file.getPath());
			}
		});
		
		chooseImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(IJ.getImage()==null) {
					JFileChooser jfc=new JFileChooser();
					jfc.setCurrentDirectory(new File(GlobalValue.URL));
					jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jfc.showDialog(new JLabel(), "choose");
					File file=jfc.getSelectedFile();
					imageUrl.setText(file.getPath());
					tempImage = new ImagePlus(file.getPath());
				}else {
					imageUrl.setText(IJ.getImage().getTitle());
					tempImage = IJ.getImage();
				}
			}
		});
		
		chooseUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setCurrentDirectory(new File(GlobalValue.URL));
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.showDialog(new JLabel(), "choose");
				File file=jfc.getSelectedFile();
				upload.setText(file.getPath());
			}
		});
		
		validation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(()->{
					LogIn.show("begin val");
					
					new FileSaver(tempImage).saveAsTiff(GlobalValue.URL+"/cache/"+tempImage.getShortTitle()+".tif");
					
					String pdStr = log.getText(), excelStr = upload.getText();
					
					if(!new File(GlobalValue.URL+"/cache/visualization.py").exists()) {
		            	FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("visualization.py"), GlobalValue.URL+"/cache/visualization.py");
		            }
					if(!new File(GlobalValue.URL+"/cache/config.py").exists()) {
		            	FileCopyUtils.fileCopy(this.getClass().getClassLoader().getResourceAsStream("config.py"), GlobalValue.URL+"/cache/config.py");
		            }
					new File(GlobalValue.URL+"/dl").mkdirs();
					new FileSaver(tempImage).saveAsTiff(GlobalValue.URL+"\\dl\\test.tif");
					String cmd = "python "+GlobalValue.URL+"\\cache\\visualization.py ";
					cmd += " --TEST_IMAGE_PATH="+GlobalValue.URL+"\\dl\\test.tif";
					cmd += " --TEST_OUT_PATH="+GlobalValue.URL+"\\dl\\";
					cmd += " --TEST_PB_PATH="+pdStr;
					cmd += " --TEST_COLOR_URL="+ excelStr;
					grayMap = new HashMap<>();
					try {
						String[][] excelStrArr = ExcelTools.getExcelTools().getData(new File(excelStr), 1);
						for(int i=0;i<excelStrArr.length;i++) {
							grayMap.put(i+1, excelStrArr[i][0]);
						}
					} catch (Exception e2) {
						LogIn.error(e2.getMessage());
					}
					
					RegistarionTools.deformation(cmd);
					
					lineImage = new ImagePlus(GlobalValue.URL+"/dl/out_line.tif");
					annotationImage = new ImagePlus(GlobalValue.URL+"/dl/out_org.tif");
					int lenX = lineImage.getWidth(),lenY = lineImage.getHeight(), lenZ = lineImage.getStackSize();
					fuseImage = NewImage.createRGBImage("fuse", lenX, lenY, lenZ, NewImage.FILL_BLACK);
					ImageStack lineImageStack = lineImage.getImageStack();
					ImageStack fuseImageStack = fuseImage.getImageStack();
					ImageStack tempImageStack = tempImage.getImageStack();
					
					RGBStackMerge rgbStackMerge = new RGBStackMerge();
					fuseImageStack = rgbStackMerge.mergeStacks(lenX, lenY, lenZ, tempImageStack, lineImageStack,null, true);
					fuseImage.setStack(fuseImageStack);
					fuseImage.show();
					fuseImage.getCanvas().addMouseListener(mouseListener);
					fuseImage.getCanvas().addMouseMotionListener(mouseMotionListener);
					LogIn.show("end val");
				}) .start();
				
			}
		});
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		int currentSlice = fuseImage.getCurrentSlice(); 
		Point tempPoint = fuseImage.getCanvas().getCursorLoc();
		xLabel.setText(tempPoint.x+"");
		yLabel.setText(tempPoint.y+"");
		int val = (int)(annotationImage.getImageStack().getVoxel(tempPoint.x, tempPoint.y, currentSlice-1));
		if(val==0) classNameLabel.setText("nil");
		else classNameLabel.setText(grayMap.get(val));
	}

	public void mouseExited(MouseEvent e) {
		xLabel.setText("nil");
		yLabel.setText("nil");
		classNameLabel.setText("nil");
	}

	public void mouseDragged(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	
}
