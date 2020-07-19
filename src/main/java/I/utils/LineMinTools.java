package I.utils;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.WandToolOptions;
import ij.process.ByteProcessor;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

public class LineMinTools {

	public boolean num(int[][] p){
		int len = 0;
		for(int x=0;x<3;x++){
			for(int y=0;y<3;y++){
				if(p[x][y]!=0){
					len++;
					if(len>=4) return true;
				}
			}
		}
		return false;
	}
	
	public boolean updateZhang(int[][] p){
		if(!num(p)) return true;
		if(p[0][0]*p[0][1]*p[1][0]!=0||p[0][1]*p[0][2]*p[1][2]!=0
		||p[1][0]*p[2][0]*p[2][1]!=0||p[2][1]*p[2][2]*p[1][2]!=0){
			return false;
		}
		if((p[0][1]*p[1][0]!=0&&p[2][2]==0)||(p[0][1]*p[1][2]!=0&&p[2][0]==0)||
				(p[1][0]*p[2][1]!=0&&p[0][2]==0)||(p[2][1]*p[1][2]!=0&&p[0][0]==0)){
			return false;
		}
		return true;
	}
	
	public boolean filter(int[][] p){
		int len = 0;
		for(int x=0;x<3;x++){
			for(int y=0;y<3;y++){
				if(p[x][y]!=0){
					len++;
					if(len>2) return true;
				}
			}
		}
		return false;
	}
	
	public int BP1(int[][] p){
		int out = 0;
		for(int x=0;x<3;x++){
			for(int y=0;y<3;y++){
				if(x==1&&y==1) continue;
				if(p[x][y]!=0) out++;
			}
		}
		return out;
	}
	
	public int AP1(int[][] p){
		int out = 0;
		if(p[0][0]==0&&p[0][1]!=0) out++;
		if(p[0][1]==0&&p[0][2]!=0) out++;
		if(p[0][2]==0&&p[1][2]!=0) out++;
		if(p[1][2]==0&&p[2][2]!=0) out++;
		if(p[2][2]==0&&p[2][1]!=0) out++;
		if(p[2][1]==0&&p[2][0]!=0) out++;
		if(p[2][0]==0&&p[1][0]!=0) out++;
		if(p[1][0]==0&&p[0][0]!=0) out++;
		return out;
	}
	
	public boolean chooseDelete(int[][] p,int type){
		boolean result = true;
		if(BP1(p)>6||BP1(p)<2) return false;
		if(AP1(p)!=1) return false;
		if(type==0){
			if(p[0][1]*p[1][2]*p[1][0]!=0||
					p[0][1]*p[2][1]*p[1][0]!=0)
				return false;
		}
		if(type==1){
			if(p[0][1]*p[1][2]*p[2][1]!=0||
					p[1][2]*p[2][1]*p[1][0]!=0)
				return false;
		}
		return result;
	}
	
	public ByteProcessor autoLineOut(ByteProcessor in,int[] inL){
		Wand w = new Wand(in);
		for(int x=3;x<inL[0]-3;x++){
			for(int y=3;y<inL[1]-3;y++){
				//if(location[x][y]==255) continue;
				if(in.get(x, y)!=0) continue;
				if((in.get(x-1, y)+in.get(x-2, y)+in.get(x-3, y))*
					(in.get(x+1, y)+in.get(x+2, y)+in.get(x+3, y))*
					(in.get(x, y-1)+in.get(x, y-2)+in.get(x, y-3))*
					(in.get(x, y+1)+in.get(x, y+2)+in.get(x, y+3))==0){
						continue;
					}
				
				w.autoOutline(x, y);
				WandToolOptions.setStart(x, y);
				
				Polygon poly = new Polygon(w.xpoints, w.ypoints, w.npoints);
				PolygonRoi polygonRoi = new PolygonRoi(poly, Roi.TRACED_ROI);

				Point[] pVector = polygonRoi.getContainedPoints();
				
				
				if(pVector.length<=7){
					for(Point p:pVector){
						in.putPixel(p.x, p.y, 255);
					}
				}
			}
		}
		
		return in;
	}
	
	public boolean fillRect(int[][] in){
		int all=0;
		for(int x=1;x<4;x++){
			for(int y=1;y<4;y++){
				if(in[y][x]!=0) all++;
			}
		}
		if(all<2) return false;
		if(in[1][2]*in[2][1]!=0){
			if((in[2][3]!=0||in[2][4]!=0)&&(in[3][2]!=0||in[4][2]!=0)) return true;
		}
		if(in[3][2]*in[2][1]!=0){
			if((in[2][3]!=0||in[2][4]!=0)&&(in[1][2]!=0||in[0][2]!=0)) return true;
		}
		if(in[3][2]*in[2][3]!=0){
			if((in[2][0]!=0||in[2][1]!=0)&&(in[1][2]!=0||in[0][2]!=0)) return true;
		}
		if(in[1][2]*in[2][3]!=0){
			if((in[2][0]!=0||in[2][1]!=0)&&(in[3][2]!=0||in[4][2]!=0)) return true;
		}
		return false;
	}
	
	public ByteProcessor fillRectAll(ByteProcessor byteProcessor,int[] d){
		List<Point> deletePoint = new ArrayList<Point>();
		int[][] p = new int[5][5];
		for(int x=2;x<d[0]-2;x++){
			for(int y=2;y<d[1]-2;y++){
				if(byteProcessor.getPixel(x, y)==0){
					p[0][2] = byteProcessor.get(x,y-2);
					p[1][2] = byteProcessor.get(x,y-1);
					p[3][2] = byteProcessor.get(x,y+1);
					p[4][2] = byteProcessor.get(x,y+2);
					p[2][0] = byteProcessor.get(x-2,y);
					p[2][1] = byteProcessor.get(x-1,y);
					p[2][3] = byteProcessor.get(x+1,y);
					p[2][4] = byteProcessor.get(x+2,y);
					if(fillRect(p)){
						Point inPoint = new Point(x, y);
						deletePoint.add(inPoint);
					}
				}
			}
		}
		for(Point point:deletePoint){
			byteProcessor.putPixel(point.x, point.y, 255);
		}
		return byteProcessor;
	}
	
	public ByteProcessor lineMin(ByteProcessor byteProcessor,int[] d){
		List<Point> deletePoint = new ArrayList<Point>();
		int type = 1;
		int[][] p = new int[3][3];
		while(true&&type<20){
			for(int x=1;x<d[0]-1;x++){
				for(int y=1;y<d[1]-1;y++){
					if(byteProcessor.get(x, y)!=0){
						p[0][0] = byteProcessor.get(x-1,y-1);
						p[0][1] = byteProcessor.get(x,y-1);
						p[0][2] = byteProcessor.get(x+1,y-1);
						p[1][0] = byteProcessor.get(x-1,y);
						p[1][1] = byteProcessor.get(x,y);
						p[1][2] = byteProcessor.get(x+1,y);
						p[2][0] = byteProcessor.get(x-1,y+1);
						p[2][1] = byteProcessor.get(x,y+1);
						p[2][2] = byteProcessor.get(x+1,y+1);
						if(chooseDelete(p, type%2)){
							Point point = new Point(x, y);
							deletePoint.add(point);
						}
					}
				}
			}
			type++;
			if(deletePoint.size()==0) break;
			for(Point point:deletePoint){
				byteProcessor.putPixel(point.x, point.y, 0);
			}
		}
		
		int num=0;
		while(true){
			for(int x=1;x<d[0]-1;x++){
				for(int y=1;y<d[1]-1;y++){
					if(byteProcessor.get(x, y)!=0){
						p[0][0] = byteProcessor.get(x-1,y-1);
						p[0][1] = byteProcessor.get(x,y-1);
						p[0][2] = byteProcessor.get(x+1,y-1);
						p[1][0] = byteProcessor.get(x-1,y);
						p[1][1] = byteProcessor.get(x,y);
						p[1][2] = byteProcessor.get(x+1,y);
						p[2][0] = byteProcessor.get(x-1,y+1);
						p[2][1] = byteProcessor.get(x,y+1);
						p[2][2] = byteProcessor.get(x+1,y+1);
						if(!filter(p)) {byteProcessor.putPixel(x, y, 0);num++;}
					}
				}
			}
			if(num==0) break;
			else num=0;
		}
		return byteProcessor;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		String url = "I:\\han\\11_6_result\\line.tif";
		ImagePlus line = new ImagePlus(url);
		line.show();
		int lenX = line.getWidth(), lenY = line.getHeight(), lenZ = line.getStackSize();
		ImagePlus lineMin = NewImage.createByteImage("lineMin", lenX, lenY, lenZ, NewImage.FILL_BLACK);
		ImageStack lineMinStack = lineMin.getImageStack();
		ImageStack lineStack = line.getImageStack();
		LineMinTools lineMinTool = new LineMinTools();
		for(int z=1;z<=lenZ;z++) {
			ByteProcessor temp = lineStack.getProcessor(z).convertToByteProcessor();
			temp = lineMinTool.lineMin(temp, line.getDimensions());
			temp = lineMinTool.autoLineOut(temp, line.getDimensions());
			temp = lineMinTool.lineMin(temp, line.getDimensions());
			lineMinStack.setProcessor(temp, z);
		}
		lineMin.setStack(lineMinStack);
		lineMin.show();
	}
}
