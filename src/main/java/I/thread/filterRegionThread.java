package I.thread;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.WandToolOptions;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.Polygon;
import java.util.concurrent.CountDownLatch;

public class filterRegionThread extends Thread{

	public ImagePlus[] imageList;
	public int location;
	public CountDownLatch countDownLatch;
	
	@Override
	public void run(){
		Binary b = new Binary();
		b.setup("open", imageList[location]);
		ImageStack saveStack = imageList[location].getImageStack();
		int radius = 3;
		boolean[][] judge = new boolean[imageList[location].getHeight()][imageList[location].getWidth()];
		for(int i=1;i<=saveStack.getSize();i++){
			for(int x=0;x<imageList[location].getWidth();x++){
				for(int y=0;y<imageList[location].getHeight();y++){
					judge[y][x] = false;
				}
			}
			ImageProcessor in = saveStack.getProcessor(i);
			b.run(in);
			in = filterRegionThread.autoLineOut(in, judge);
			in = filterRegionThread.medianBinary(in, imageList[location].getDimensions(), radius);
			saveStack.setProcessor(in, i);
		}
		imageList[location].setStack(saveStack);
//		FileSaver fixSaver = new FileSaver(imageList[location]);
//		fixSaver.saveAsTiff(url);
		countDownLatch.countDown();
	}
	
	public filterRegionThread(ImagePlus[] imageList, int location, CountDownLatch countDownLatch){
		this.imageList = imageList;
		this.location = location;
		this.countDownLatch = countDownLatch;
	}
	
	public static ImageProcessor autoLineOut(ImageProcessor in,boolean[][] judge){
		Wand w = new Wand(in);
		for(int x=3;x<judge[0].length-3;x++){
			for(int y=3;y<judge.length-3;y++){
				if(in.get(x, y)==0||judge[y][x]) continue;
				
				w.autoOutline(x, y);
				WandToolOptions.setStart(x, y);
				
				Polygon poly = new Polygon(w.xpoints, w.ypoints, w.npoints);
				PolygonRoi polygonRoi = new PolygonRoi(poly, Roi.TRACED_ROI);

				Point[] pVector = polygonRoi.getContainedPoints();
				
				if(pVector.length<=30){
					for(Point p:pVector){
						in.putPixel(p.x, p.y, 0);
					}
				}
				for(Point p:pVector){
					judge[p.y][p.x] = true;
				}
			}
		}
		
		return in;
	}
	public static ImageProcessor medianBinary(ImageProcessor in,int[] dem,int radius){
		int half = (radius*radius+1)/2,r = (radius-1)/2;
		int[][] judge = new int[dem[1]-2*r][dem[0]-2*r];
		judge[0][0] = 0;
		for(int x=0;x<radius;x++){
			for(int y=0;y<radius;y++){
				if(in.get(x, y)>0) judge[0][0]++;
			}
		}
		
		for(int y=1;y<dem[1]-2*r;y++){
			judge[y][0] = judge[y-1][0];
			for(int l=0;l<radius;l++){
				if(in.get(l, y+2*r)>0) judge[y][0]++;
				if(in.get(l, y-1)>0) judge[y][0]--;
			}
		}
		
		for(int x=1;x<dem[0]-2*r;x++){
			for(int y=0;y<dem[1]-2*r;y++){
				 judge[y][x] = judge[y][x-1];
				 for(int l=y;l<radius+y;l++){
					 if(in.get(x+2*r, l)>0) judge[y][x]++;
					 if(in.get(x-1, l)>0) judge[y][x]--;
				 }
			}
		}
	
		for(int x=r;x<dem[0]-r;x++){
			for(int y=r;y<dem[1]-r;y++){
				if(judge[y-r][x-r]<half) in.putPixel(x, y, 0);
				else in.putPixel(x, y, 255);
			}
		}
		return in;
	}
}
