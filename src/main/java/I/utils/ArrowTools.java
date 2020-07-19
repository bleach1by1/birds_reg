package I.utils;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import I.entity.MaskPointEntity;
import ij.ImagePlus;
import ij.process.ColorProcessor;

/**
 * 
 * @author zwl
 *
 */
public class ArrowTools {
	
	 public static ColorProcessor drawLine(int x1, int y1, int x2, int y2, ColorProcessor imageProcessor,int channel) {
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int sgnX = x1 < x2 ? 1 : -1;
		int sgnY = y1 < y2 ? 1 : -1;
		int e = 0;
		for (int i=0; i < dx+dy; i++) {
			switch (channel) {
				case 1: imageProcessor.putPixel(x1, y1, new int[]{0,0,255}); break;
				case 2: imageProcessor.putPixel(x1, y1, new int[]{0,255,0}); break;
				case 3: imageProcessor.putPixel(x1, y1, new int[] {255,0,0}); break;
			}
			int e1 = e + dy;
			int e2 = e - dx;
			if (Math.abs(e1) < Math.abs(e2)) {
				x1 += sgnX;
				e = e1;
			} else {
				y1 += sgnY;
				e = e2;
			}
		}
		
		switch (channel) {
			case 1: imageProcessor.putPixel(x2, y2, new int[]{0,0,255}); break;
			case 2: imageProcessor.putPixel(x2, y2, new int[]{0,255,0}); break;
			case 3: imageProcessor.putPixel(x2, y2, new int[] {255,0,0}); break;
		}
		
		return imageProcessor;
		}
	
	
	 public static List<MaskPointEntity> drawLinePoint(int x1, int y1, int x2, int y2, ImagePlus imagePlus) {
		List<MaskPointEntity> out = new ArrayList<MaskPointEntity>();
		MaskPointEntity mPointEntity;
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int sgnX = x1 < x2 ? 1 : -1;
		int sgnY = y1 < y2 ? 1 : -1;
		int e = 0;
		for (int i=0; i < dx+dy; i++) {
//			putPixel(x1, y1, fgColor);
			//putPixel(x1, y1, 255);
			//Point add = new Point(x1, y1);
			//out.add(add);
			mPointEntity = new MaskPointEntity();
			mPointEntity.x = x1;
			mPointEntity.y = y1;
			mPointEntity.r = imagePlus.getPixel(x1, y1)[0];
			mPointEntity.g = imagePlus.getPixel(x1, y1)[1];
			mPointEntity.b = imagePlus.getPixel(x1, y1)[2];
			out.add(mPointEntity);
			
			int e1 = e + dy;
			int e2 = e - dx;
			if (Math.abs(e1) < Math.abs(e2)) {
				x1 += sgnX;
				e = e1;
			} else {
				y1 += sgnY;
				e = e2;
			}
		}
		
		//Point add = new Point(x2, y2);
		//out.add(add);
		mPointEntity = new MaskPointEntity();
		mPointEntity.x = x2;
		mPointEntity.y = y2;
		mPointEntity.r = imagePlus.getPixel(x2, y2)[0];
		mPointEntity.g = imagePlus.getPixel(x2, y2)[1];
		mPointEntity.b = imagePlus.getPixel(x2, y2)[2];
		out.add(mPointEntity);
		return out;
//		putPixel(x2, y2, fgColor);
	//	putPixel(x2, y2, 255);
	}
	
	public List<Point> lineChoose(int beginX,int beginY,int endX,int endY){
		int begin,end;
		double locationX,locationY;
		int dx = endX-beginX;
		int dy = endY-beginY;
		double k = dy/dx;
		double b = (endY*beginX-endX*beginY)/(beginX-endX);
		List<Point> out = new ArrayList<>();
		Point point;
		
		if(Math.abs(dx)>Math.abs(dy)){
			if(dx>0){
				begin = beginX;
				end = endX;
			}else{
				begin = endX;
				end = beginX;
			}
			for(int x=begin;x<end;x++){
				locationX = x;
				locationY = k*x+b;
				point = new Point();
				point.setLocation(Math.round(locationX), Math.round(locationY));
				out.add(point);
			}
		}else{
			if(dy>0){
				begin = beginY;
				end = endY;
			}else{
				begin = endY;
				end = beginY;
			}
			for(int y=begin;y<end;y++){
				locationY = y;
				locationX = (y-b)/k;
				point = new Point();
				point.setLocation(Math.round(locationX), Math.round(locationY));
				out.add(point);
			}
		}
		
		return out;
	}
}
