package I.utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.process.ImageProcessor;

public class BoundaryTools {
	
	public static int MAXTHREAD = 50;
	
	/**
	 * 
	 * @param in want draw line image
	 * @param color 0:black, 1:white
	 * @return line ImagePlus
	 */
	public static ImagePlus getBorder(ImagePlus in, int color) {
		int lenX = in.getWidth(), lenY = in.getHeight(), lenZ = in.getStackSize(),judge = color==0?0:255;
		ImagePlus out = NewImage.createImage("line", lenX, lenY, lenZ, in.getBitDepth(), NewImage.FILL_BLACK);
		ImageStack inStack = in.getImageStack(), outStack = out.getImageStack();
		
		int iter = lenZ/MAXTHREAD+(lenZ%MAXTHREAD>0?1:0);
		for(int i=0;i<iter;i++) {
			int num = (lenZ-i*MAXTHREAD)>MAXTHREAD?MAXTHREAD:(lenZ-i*MAXTHREAD);
			CountDownLatch count = new CountDownLatch(num);
			for(int z=i*MAXTHREAD;z<i*MAXTHREAD+num;z++) {
				final int zz = z;
				new Thread(()->{
					//two judgments up and left
					for(int y=0;y<lenY;y++) {
						boolean sw = true;
						for(int x=0;x<lenX;x++) {
							int val = (int)inStack.getVoxel(x, y, zz);
							if(sw) {
								if(val!=judge) {outStack.setVoxel(x, y, zz, 255);sw = false;}
							}else {
								if(val==judge) {outStack.setVoxel(x, y, zz, 255);sw = true;}
							}
						}
					}
					
					for(int x=0;x<lenX;x++) {
						boolean sw = true;
						for(int y=0;y<lenY;y++) {
							int val = (int)inStack.getVoxel(x, y, zz);
							if(sw) {
								if(val!=judge) {outStack.setVoxel(x, y, zz, 255);sw = false;}
							}else {
								if(val==judge) {outStack.setVoxel(x, y, zz, 255);sw = true;}
							}
						}
					}
					count.countDown();
				}) .start();
			}
			
			try {
				count.await();
			} catch (Exception e) {
				IJ.log(e.getMessage());
			}
		}
		
		out.setStack(outStack);
		return out;
	}
	
	/**
	 * 
	 * @param image process of boundary
	 * @param r boundary's rectangle
	 * @param value region value
	 * @return point's arrayList
	 */
	public static List<Point> getBoundaryPoints(ImageProcessor image,Rectangle r,int value){
		List<Point> out = new ArrayList<>();
		int lenX = r.x+r.width,lenY = r.y+r.height;
		for(int xx=r.x;xx<lenX;xx++){
			int time = 0;
			for(int yy=r.y;yy<lenY;yy++){
				int val = (int)image.getPixelValue(xx, yy);
				if(time==0&&val==value){
					out.add(new Point(xx, yy));time++;
				}else if(time==1&&(val==value&&(yy+1>=lenY||(int)image.getPixelValue(xx, yy+1)!=value))){
					out.add(new Point(xx, yy));time = 0;
				}
			}
		}
		
		for(int yy=r.y;yy<lenY;yy++){
			int time = 0;
			for(int xx=r.x;xx<lenX;xx++){
				int val = (int)image.getPixelValue(xx, yy);
				if(time==0&&val==value){
					out.add(new Point(xx, yy));time++;
				}else if(time==1&&(val==value&&(xx+1>=lenX||(int)image.getPixelValue(xx+1, yy)!=value))){
					out.add(new Point(xx, yy));time = 0;
				}
			}
		}
		return out;
	}
}
