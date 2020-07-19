 package I.precise.tools;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.WandToolOptions;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import I.utils.BoundaryTools;

public class LineTools {

	private LineTools(){}
	
	private volatile static LineTools lineTool = null;
	
	public static LineTools getLineTool(){
		if(lineTool==null){
			synchronized (LineTools.class) {
				if(lineTool==null){
					lineTool = new LineTools();
				}
			}
		}
		return lineTool;
	}
	
	public ImageProcessor lineMinGet(ImageProcessor imageProcessor, ImageProcessor outProcessor) {
		int lenX = imageProcessor.getWidth(), lenY = imageProcessor.getHeight();
		for(int x=0;x<lenX;x++) {
			int begin = 0;
			for(int y=0;y<lenY;y++) {
				int temp = (int)imageProcessor.getPixelValue(x, y);
				if(temp!=begin) {
					outProcessor.set(x, y, 255);
					begin = temp;
				}
				if(y==lenY-1&&begin!=0) {
					outProcessor.set(x, y, 255);
				}
			}
		}
		
		for(int y=0;y<lenY;y++) {
			int begin = 0;
			for(int x=0;x<lenX;x++) {
				int temp = (int)imageProcessor.getPixelValue(x, y);
				if(temp!=begin) {
					outProcessor.set(x, y, 255);
					begin = temp;
				}
				if(x==lenX-1&&begin!=0) {
					outProcessor.set(x, y, 255);
				}
			}
		}
		
		return outProcessor;
	}
	
	public Map<Integer, List<Rectangle>> getBoxAllSlice(ImageProcessor imageProcessor){
		Map<Integer, List<Rectangle>> out = new HashMap<>();
		int lenX = imageProcessor.getWidth(), lenY = imageProcessor.getHeight();
		int[][] judge = new int[lenY][lenX];
		Wand w = new Wand(imageProcessor);
		for(int x=0;x<lenX;x++){ 
			for(int y=0;y<lenY;y++){
				if(judge[y][x]!=(int)imageProcessor.getPixelValue(x, y)&&(int)imageProcessor.getPixelValue(x, y)>0){
					int val = (int)imageProcessor.getPixelValue(x, y);
						
					w.autoOutline(x, y);
					WandToolOptions.setStart(x, y);
							
					Polygon poly = new Polygon(w.xpoints, w.ypoints, w.npoints);
					//Rectangle temp = new Rectangle();
					Rectangle temp = poly.getBounds();
					PolygonRoi polygonRoi = new PolygonRoi(poly, Roi.TRACED_ROI);
					Point[] pVector = polygonRoi.getContainedPoints();
					for(Point p:pVector){
						judge[p.y][p.x] = val;
					}
					System.out.println(pVector.length);
					if(temp.width==lenX&&temp.height==lenY) continue;
					if(!out.containsKey(val)) out.put(val, new ArrayList<>());
					out.get(val).add(temp);
				}
			}
		}
		return out;
	}
	
	public ImageProcessor stackLineGet(ImageProcessor imageProcessor, ImageProcessor outProcessor, Map<Integer, Rectangle> roiMap){
		for(Map.Entry<Integer, Rectangle> m:roiMap.entrySet()){
			List<Point> temp = BoundaryTools.getBoundaryPoints(imageProcessor, m.getValue(), m.getKey());
			for(Point p:temp){
				outProcessor.putPixel(p.x, p.y, 255);
			}
		}
		return outProcessor;
	}
	
}
