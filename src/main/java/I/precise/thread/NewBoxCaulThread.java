package I.precise.thread;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import I.utils.MedianTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.WandToolOptions;
import ij.process.ImageProcessor;

public class NewBoxCaulThread implements Runnable{

	private Map<Integer, Rectangle> boxMap;
	private Map<Integer, List<Rectangle>> boxAllMap;
	private Map<Integer, ImageStack> tempImageStackMap;
	private Integer val;
	private String url;
	private CountDownLatch countDownLatch;
	
	public NewBoxCaulThread(Map<Integer, Rectangle> boxMap,Map<Integer, List<Rectangle>> boxAllMap,Map<Integer, ImageStack> tempImageStackMap, Integer val,
			String url, CountDownLatch countDownLatch) {
		this.boxMap = boxMap;
		this.boxAllMap = boxAllMap;
		this.tempImageStackMap = tempImageStackMap;
		this.val = val;
		this.url = url;
		this.countDownLatch = countDownLatch;
	}
	
	@Override
	public void run() {
		
		ImagePlus tempGetImage = new ImagePlus(url);
		ImageProcessor readProcessor = tempGetImage.getImageStack().getProcessor(1);
		int max = 0, location = 0;
		int[] tempArr = readProcessor.getHistogram();
		for(int t=0;t<tempArr.length;t++) {
			if(tempArr[t]>max) {location = t;max = tempArr[t];}
		}
		int lenX = readProcessor.getWidth(), lenY = readProcessor.getHeight();
		for(int x=0;x<lenX;x++) {
			for(int y=0;y<lenY;y++) {
				if(readProcessor.getPixelValue(x, y)>location) {
					readProcessor.set(x, y, 255);
				}else {
					readProcessor.set(x, y, 0);
				}
			}
		}
		
		Wand w = new Wand(readProcessor);
		readProcessor = MedianTools.medianBinary(readProcessor, new int[] {lenX, lenY},3);
		int minX = Integer.MAX_VALUE,minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
		boolean[][] judge = new boolean[lenY][lenX];
		for(int x=0;x<lenX;x++) {
			for(int y=0;y<lenY;y++) {
				if(readProcessor.getPixelValue(x, y)>location) {
					if(!judge[y][x]) {
						w.autoOutline(x, y);
						WandToolOptions.setStart(x, y);
						Polygon poly = new Polygon(w.xpoints, w.ypoints, w.npoints);
						Rectangle temp = poly.getBounds();
						minX = Integer.min(minX, temp.x);
						minY = Integer.min(minY, temp.y);
						maxX = Integer.max(maxX, temp.x+temp.width);
						maxY = Integer.max(maxY, temp.y+temp.height);
						PolygonRoi polygonRoi = new PolygonRoi(poly, Roi.TRACED_ROI);
						if(!boxAllMap.containsKey(val)) boxAllMap.put(val, new ArrayList<>());
						boxAllMap.get(val).add(temp);
						Point[] pVector = polygonRoi.getContainedPoints();
						for(Point p:pVector){
							judge[p.y][p.x] = true;
						}
					}
				}
			}
		}
		
		ImagePlus outTempImage = NewImage.createByteImage("", lenX, lenY, 1, NewImage.FILL_BLACK);
		ImageStack outTempImageStack = outTempImage.getImageStack();
		outTempImageStack.setProcessor(readProcessor, 1);
		
		tempImageStackMap.put(val, outTempImageStack);
		if(minX==Integer.MAX_VALUE&&minY==Integer.MAX_VALUE) {
			boxMap.remove(val);
			countDownLatch.countDown();
			return;
		}
		boxMap.put(val, new Rectangle(minX, minY, maxX-minX, maxY-minY));
		
		countDownLatch.countDown();
	}
	
}
