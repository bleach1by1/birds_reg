package I.deeplearning;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.plugin.PlugIn;

public class CutImage implements PlugIn{
	
	public static boolean JUDGE = false; 

	public JFrame frame;
	
	public JTextField x00Field,y00Field,x01Field,y01Field,x10Field,y10Field,x11Field,y11Field,rawField,annField,outField;
	public JComboBox<String> chooseBox;
	public Button rawButton,annButton,outButton,okButton;
	
	public void location() {
		frame = new JFrame("cut image");
		frame.setBounds(300, 400, 450, 280);
		frame.setLayout(null);
		
		int beginX = 30,beginY = 10,high = 20;
		JLabel rawLabel = new JLabel("raw url:");
		rawField = new JTextField(50);
		rawButton = new Button("choose");
		rawLabel.setBounds(beginX, beginY, 80, high);
		rawField.setBounds(beginX+=85, beginY, 200, high);
		rawButton.setBounds(beginX+=210, beginY, 60, high);
		frame.add(rawLabel);
		frame.add(rawField);
		frame.add(rawButton);
		
		beginX = 30;beginY+=30;
		JLabel annLabel = new JLabel("ann url:");
		annField = new JTextField(50);
		annButton = new Button("choose");
		annLabel.setBounds(beginX, beginY, 80, high);
		annField.setBounds(beginX+=85, beginY, 200, high);
		annButton.setBounds(beginX+=210, beginY, 60, high);
		frame.add(annLabel);
		frame.add(annField);
		frame.add(annButton);
		
		beginX = 130;beginY+=30;
		JLabel modelLabel = new JLabel("model:");
		chooseBox = new JComboBox<>();
		chooseBox.addItem("up and down");
		chooseBox.addItem("left and right");
		modelLabel.setBounds(beginX, beginY, 70, high);
		chooseBox.setBounds(beginX+=70, beginY, 100, high);
		frame.add(modelLabel);
		frame.add(chooseBox);
		
		beginX = 60;beginY+=30;
		JLabel x00Label = new JLabel("x00:");
		x00Field = new JTextField(20);
		JLabel y00Label = new JLabel("y00:");
		y00Field = new JTextField(20);
		JLabel x01Label = new JLabel("x01:");
		x01Field = new JTextField(20);
		JLabel y01Label = new JLabel("y01:");
		y01Field = new JTextField(20);
		x00Label.setBounds(beginX, beginY,25, high);
		x00Field.setBounds(beginX+=30, beginY,40, high);
		y00Label.setBounds(beginX+=50, beginY,25, high);
		y00Field.setBounds(beginX+=30, beginY,40, high);
		x01Label.setBounds(beginX+=50, beginY,25, high);
		x01Field.setBounds(beginX+=30, beginY,40, high);
		y01Label.setBounds(beginX+=50, beginY,25, high);
		y01Field.setBounds(beginX+=30, beginY,40, high);
		frame.add(x00Label);
		frame.add(x00Field);
		frame.add(y00Label);
		frame.add(y00Field);
		frame.add(x01Label);
		frame.add(x01Field);
		frame.add(y01Label);
		frame.add(y01Field);
		
		beginX = 60;beginY+=30;
		JLabel x10Label = new JLabel("x10:");
		x10Field = new JTextField(20);
		JLabel y10Label = new JLabel("y10:");
		y10Field = new JTextField(20);
		JLabel x11Label = new JLabel("x11:");
		x11Field = new JTextField(20);
		JLabel y11Label = new JLabel("y11:");
		y11Field = new JTextField(20);
		x10Label.setBounds(beginX, beginY,25, high);
		x10Field.setBounds(beginX+=30, beginY,40, high);
		y10Label.setBounds(beginX+=50, beginY,25, high);
		y10Field.setBounds(beginX+=30, beginY,40, high);
		x11Label.setBounds(beginX+=50, beginY,25, high);
		x11Field.setBounds(beginX+=30, beginY,40, high);
		y11Label.setBounds(beginX+=50, beginY,25, high);
		y11Field.setBounds(beginX+=30, beginY,40, high);
		frame.add(x10Label);
		frame.add(x10Field);
		frame.add(y10Label);
		frame.add(y10Field);
		frame.add(x11Label);
		frame.add(x11Field);
		frame.add(y11Label);
		frame.add(y11Field);
		
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
		okButton.setBounds(beginX, beginY, 40, high);
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
		
		rawButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "raw");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				rawField.setText(URL);
			}
		});
		
		annButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "ann");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				annField.setText(URL);
			}
		});
		
		outButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				jfc.showDialog(new JLabel(), "out");
				File file=jfc.getSelectedFile();
				String URL = file.getPath();
				outField.setText(URL);
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if(!JUDGE) {
					new Thread(()->{
						JUDGE = true;
						try {
							int model = chooseBox.getSelectedIndex();
							ImagePlus raw = new ImagePlus(rawField.getText());
							System.out.println(raw.getTitle());
							switch (model) {
							case 0:model0(); break;
							case 1:model1(); break;
							default:break;
							}
						} catch (Exception e2) {
							IJ.log(e2.getMessage());
						}finally {
							JUDGE = false;
						}
						JUDGE = false;
					}).start();
				}else {
					JOptionPane.showMessageDialog(null, "this is running", "error",JOptionPane.PLAIN_MESSAGE);
				}
				
				
			}
		});
	}
	
	private void model1() {
		String rawUrl = rawField.getText(),annUrl = annField.getText();
		ImagePlus raw = new ImagePlus(rawUrl);
		ImagePlus ann = new ImagePlus(annUrl);
		int lenX = raw.getWidth(),lenY = raw.getHeight(), lenZ = raw.getStackSize();
		ImageStack rawStack = raw.getImageStack(), annStack = ann.getImageStack();
		String x00Str = x00Field.getText(),x01Str = x01Field.getText(),y00Str = y00Field.getText(),y01Str = y01Field.getText();
		String x10Str = x10Field.getText(),x11Str = x11Field.getText(),y10Str = y10Field.getText(),y11Str = y11Field.getText();
		String out = outField.getText();
		if(x00Str.length()>0&&x01Str.length()>0&&y00Str.length()>0&&y01Str.length()>0) {
			double x00 = Double.valueOf(x00Str),x01 = Double.valueOf(x01Str),y00 = Double.valueOf(y00Str),y01 = Double.valueOf(y01Str);
			double a = (y01-y00)/(x01-x00), b = y00-a*x00;
			for(int y=0;y<lenY;y++){
				int beginX = (int)Math.round(((double)y-b)/a);
				for(int z=0;z<lenZ;z++){
					for(int x=0;x<beginX;x++){
						rawStack.setVoxel(x, y, z, 0);
						annStack.setVoxel(x, y, z, 0);
					}
				}
			}
		}
		
		if(x10Str.length()>0&&x11Str.length()>0&&y10Str.length()>0&&y11Str.length()>0) {
			double x10 = Double.valueOf(x10Str),x11 = Double.valueOf(x11Str),y10 = Double.valueOf(y10Str),y11 = Double.valueOf(y11Str);
			double a = (y11-y10)/(x11-x10), b = y10-a*x10;
			for(int y=0;y<lenY;y++){
				int beginX = (int)Math.round(((double)y-b)/a);
				for(int z=0;z<lenZ;z++){
					for(int x=beginX;x<lenX;x++){
						rawStack.setVoxel(x, y, z, 0);
						annStack.setVoxel(x, y, z, 0);
					}
				}
			}
		}
		raw.setStack(rawStack);ann.setStack(annStack);
		new FileSaver(raw).saveAsTiff(out+"/cut"+raw.getTitle());
		new FileSaver(ann).saveAsTiff(out+"/cut"+ann.getTitle());
	}
	
	private void model0() {
		String rawUrl = rawField.getText(),annUrl = annField.getText();
		ImagePlus raw = new ImagePlus(rawUrl);
		ImagePlus ann = new ImagePlus(annUrl);
		int lenX = raw.getWidth(),lenY = raw.getHeight(), lenZ = raw.getStackSize();
		ImageStack rawStack = raw.getImageStack(), annStack = ann.getImageStack();
		String x00Str = x00Field.getText(),x01Str = x01Field.getText(),y00Str = y00Field.getText(),y01Str = y01Field.getText();
		String x10Str = x10Field.getText(),x11Str = x11Field.getText(),y10Str = y10Field.getText(),y11Str = y11Field.getText();
		String out = outField.getText();
		if(x00Str.length()>0&&x01Str.length()>0&&y00Str.length()>0&&y01Str.length()>0) {
			double x00 = Double.valueOf(x00Str),x01 = Double.valueOf(x01Str),y00 = Double.valueOf(y00Str),y01 = Double.valueOf(y01Str);
			double a = (y01-y00)/(x01-x00), b = y00-a*x00;
			for(int x=0;x<lenX;x++){
				int beginY = (int)Math.round(a*(double)x+b);
				for(int z=0;z<lenZ;z++){
					for(int y=0;y<beginY;y++){
						rawStack.setVoxel(x, y, z, 0);
						annStack.setVoxel(x, y, z, 0);
					}
				}
			}
		}
		
		if(x10Str.length()>0&&x11Str.length()>0&&y10Str.length()>0&&y11Str.length()>0) {
			double x10 = Double.valueOf(x10Str),x11 = Double.valueOf(x11Str),y10 = Double.valueOf(y10Str),y11 = Double.valueOf(y11Str);
			double a = (y11-y10)/(x11-x10), b = y10-a*x10;
			for(int x=0;x<lenX;x++){
				int beginY = (int)Math.round(a*(double)x+b);
				for(int z=0;z<lenZ;z++){
					for(int y=beginY;y<lenY;y++){
						rawStack.setVoxel(x, y, z, 0);
						annStack.setVoxel(x, y, z, 0);
					}
				}
			}
		}
		raw.setStack(rawStack);ann.setStack(annStack);
		new FileSaver(raw).saveAsTiff(out+"/cut"+raw.getTitle());
		new FileSaver(ann).saveAsTiff(out+"/cut"+ann.getTitle());
	}
	
	public static void main(String[] args) {
		CutImage c = new CutImage();
		c.run(null);
	}

	public void run(String arg) {
		location();
	}

}
