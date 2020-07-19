package I.utils;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import I.entity.Box3DEntity;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.WandToolOptions;
import ij.process.ImageProcessor;

public class FindBoxTools {

	private ImageProcessor image;
	private int value;
	private boolean[][] judge;
	
	public FindBoxTools(){}
	
	public static volatile FindBoxTools findBoxTools = null;
	
	public static FindBoxTools getFindBoxTools(){
		if(findBoxTools==null){
			synchronized (FindBoxTools.class) {
				if(findBoxTools==null){
					findBoxTools = new FindBoxTools();
				}
			}
		}
		return findBoxTools;
	}
	
	public FindBoxTools setImage(ImageProcessor image){
		this.image = image;
		judge = new boolean[image.getHeight()][image.getWidth()];
		return findBoxTools;
	}
	
	public FindBoxTools setValue(int value){
		this.value = value;
		return findBoxTools;
	}
	
	private Rectangle findFirstPoint(int x,int y,Wand w){
		w.autoOutline(x, y);
		WandToolOptions.setStart(x, y);
				
		Polygon poly = new Polygon(w.xpoints, w.ypoints, w.npoints);
		Rectangle r = new Rectangle();
		r = poly.getBounds();
		PolygonRoi polygonRoi = new PolygonRoi(poly, Roi.TRACED_ROI);
		Point[] pVector = polygonRoi.getContainedPoints();
		for(Point p:pVector){
			judge[p.y][p.x] = true;
		}
		return r;
	}
	
	public Box3DEntity findRegion3DBox(Map<Integer, List<Rectangle>> map) {
		Box3DEntity out = new Box3DEntity();
		int minZ = Integer.MAX_VALUE, maxZ = 0, minX = Integer.MAX_VALUE, maxX = 0,
				minY = Integer.MAX_VALUE, maxY = 0;
		for(Map.Entry<Integer, List<Rectangle>> m:map.entrySet()) {
			minZ = Math.min(minZ, m.getKey());
			maxZ = Math.max(maxZ, m.getKey());
			for(Rectangle r:m.getValue()) {
				minX = Math.min(r.x, minX);
				maxX = Math.max(maxX, r.x+r.width);
				minY = Math.min(r.y, minY);
				maxY = Math.max(maxY, r.y+r.height);
				System.out.println("rect beginx: "+r.x+" beginy: "+r.y+" endx: "+(r.x+r.width)+" endy: "+(r.y+r.height));
			}
		}
		if(minZ==Integer.MAX_VALUE||minX==Integer.MAX_VALUE||minY==Integer.MAX_VALUE) {
			return null;
		}
		out.setX(minX);
		out.setY(minY);
		out.setZ(minZ);
		out.setWidth(maxX-minX);
		out.setHeight(maxY-minY);
		out.setDepth(maxZ-minZ);
		return out;
	}
	
	public List<Rectangle> getBoxLocation(){
		Wand w = new Wand(image);
		List<Rectangle> out = new LinkedList<>();
		int lenX = image.getWidth(),lenY = image.getHeight();
		for(int y=0;y<lenY;y++){
			for(int x=0;x<lenX;x++){
				if(!judge[y][x]&&image.get(x, y)==value){
					Rectangle r = findFirstPoint(x, y, w);
					if(r.x+r.width==image.getWidth()||r.y+r.height==image.getHeight()) continue;
					out.add(r);
				}
			}
		}

		return out;
	}
	
	public ImageProcessor drawBox(Rectangle r,ImageProcessor image,int value){
		for(int x=r.x;x<=r.x+r.width;x++){
			image.set(x, r.y, value);
			image.set(x, r.y+r.height, value);
		}
		for(int y=r.y;y<=r.y+r.height;y++){
			image.set(r.x, y, value);
			image.set(r.x+r.width, y, value);
		}
		return image;
	}
	
	public Rectangle fuseBox(List<Rectangle> temp){
		int x1 = Integer.MAX_VALUE,x2 = Integer.MIN_VALUE,y1 = Integer.MAX_VALUE,y2 = Integer.MIN_VALUE;
		for(Rectangle r:temp){
			int calX1 = r.x,calX2 = r.x+r.width,calY1 = r.y,calY2 = r.y+r.height;
			x1 = Math.min(x1, calX1);x2 = Math.max(x2, calX2);
			y1 = Math.min(y1, calY1);y2 = Math.max(y2, calY2);
		}
		Rectangle out = new Rectangle(x1, y1, x2-x1, y2-y1);
		return out;
	}
	
	public boolean judgeOverlap(List<Rectangle> l1,List<Rectangle> l2){
		int lenG = l1.size(),lenT = l2.size();
		for(int g=0;g<lenG;g++){
			for(int t=0;t<lenT;t++){
				Rectangle gr = l1.get(g);
				Rectangle tr = l2.get(t);
				int gx1 = gr.x,gx2 = gr.x+gr.width,gy1 = gr.y,gy2 = gr.y+gr.height;
				int tx1 = tr.x,tx2 = tr.x+tr.width,ty1 = tr.y,ty2 = tr.y+tr.height;
				if((tx1<=gx1&&gx1<=tx2)||(gx1<=tx1&&tx1<=gx2)){
					if((ty1<=gy1&&gy1<=ty2)||(gy1<=ty1&&ty1<=gy2)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url = "D:\\data_mask_rcnn\\brain\\ISO.tif";
		new ImageJ();
		ImagePlus image = new ImagePlus(url);
		ImageStack imageStack = image.getImageStack();
		int width = imageStack.getWidth(),height = imageStack.getHeight(),slices = imageStack.getSize();
		ImagePlus boundaryImage = NewImage.createByteImage("", width, height, slices, NewImage.FILL_BLACK);
		ImageStack boundaryStack = boundaryImage.getImageStack();
		for(int s=1;s<=imageStack.getSize();s++){
			ImageProcessor temp = imageStack.getProcessor(s);
			ImageProcessor line = boundaryStack.getProcessor(s);
			List<Rectangle> tempRec = findBoxTools.getFindBoxTools().setImage(temp).setValue(255).getBoxLocation();
			for(Rectangle r:tempRec){
				temp = findBoxTools.getFindBoxTools().drawBox(r, temp,200);
			}
			Rectangle fuse = findBoxTools.getFindBoxTools().fuseBox(tempRec);
			temp = findBoxTools.getFindBoxTools().drawBox(fuse, temp,200);
			List<Point> getLinePoint = BoundaryTools.getBoundaryPoints(temp, fuse,255);
			for(Point p:getLinePoint) line.set(p.x, p.y, 250);
			imageStack.setProcessor(temp, s);
			boundaryStack.setProcessor(line, s);
		}
		image.setStack(imageStack);
		boundaryImage.setStack(boundaryStack);
		boundaryImage.show();
		image.show();
	}

}
