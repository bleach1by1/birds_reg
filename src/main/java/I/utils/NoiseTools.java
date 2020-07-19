package I.utils;

import java.awt.Point;
import java.awt.Polygon;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.WandToolOptions;

public class NoiseTools {
	public static int MAXTHREAD = 50;
	
	/**
	 * 
	 * @param in line image
	 * @param threshold remove pixel thresholds within the line
	 * @param lineVal
	 * @return filter line image
	 */
	public static ImagePlus lineNoiseRemove(ImagePlus in,int threshold,int lineVal) {
		int lenX = in.getWidth(), lenY = in.getHeight(), lenZ = in.getStackSize();
		ImageStack inStack = in.getImageStack();
		int iter = lenZ/MAXTHREAD+(lenZ%MAXTHREAD>0?1:0);
		for(int i=0;i<iter;i++) {
			int num = (lenZ-i*MAXTHREAD)>MAXTHREAD?MAXTHREAD:(lenZ-i*MAXTHREAD);
			CountDownLatch count = new CountDownLatch(num);
			for(int z=i*MAXTHREAD;z<i*MAXTHREAD+num;z++) {
				final int zz = z;
				new Thread(()->{
					Wand w = new Wand(inStack.getProcessor(zz+1));
					boolean[][] judge = new boolean[lenY][lenX];
					for(int x=0;x<lenX;x++) {
						for(int y=0;y<lenY;y++) {
							int val = (int)inStack.getVoxel(x, y, zz);
							if(val==lineVal&&!judge[y][x]) {
								w.autoOutline(x, y);
								WandToolOptions.setStart(x, y);
								Polygon poly = new Polygon(w.xpoints, w.ypoints, w.npoints);
								PolygonRoi polygonRoi = new PolygonRoi(poly, Roi.TRACED_ROI);
								Point[] pVector = polygonRoi.getContainedPoints();
								if(pVector.length>threshold) continue;
								for(Point p:pVector){
									judge[p.y][p.x] = true;
									if(pVector.length<=threshold) inStack.setVoxel(x, y, zz, 0);
								}
							}
						}
					}
					count.countDown();
				}).start();
			}
			try {
				count.await();
			} catch (Exception e) {
				IJ.log(e.getMessage());
			}
		}
		
		in.setStack(inStack);
		return in;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
