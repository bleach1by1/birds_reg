package I.utils;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.OpenDialog;
import ij.process.ImageProcessor;

public class LineObtainRegionTools {

	/*
	 * get line point
	 */
	public static ImageProcessor getRegionLinePoint(ImageProcessor byteProcessor,int[] dem){
		int lenX = dem[0],lenY=dem[1];
		int[][] location = new int[lenX][lenY];
		for(int x=0;x<lenX;x++){
			for(int y=0;y<lenY;y++){
				////if(byteProcessor.getPixel(x, y)>32768) location[x][y] = 255;
				if(byteProcessor.getPixel(x, y)>0) location[x][y] = 255;
			}
		}
		
		for(int y=1;y<lenY-1;y++){
			for(int x=1;x<lenX-1;x++){
				if(location[x][y]>0&&(location[x-1][y]==0||
						location[x-1][y-1]==0||location[x][y-1]==0||
						location[x+1][y]==0||location[x+1][y+1]==0||
						location[x][y+1]==0)){
					//shortProcessor.putPixel(x, y, 255);
					////byteProcessor.putPixel(x, y, 32768+100);
					byteProcessor.putPixel(x, y, 255);
				}else if(location[x][y]>0){
					byteProcessor.putPixel(x, y, 0);
				}

			}
		}
		
		return byteProcessor;
	}
	
	public static ImagePlus getRegionLineStack(ImagePlus in,int[] dem){
		ImageStack inImageStack = in.getImageStack();
		//Filters3D.filter(inImageStack, Filters3D.MEDIAN, dem[0], dem[1], dem[2]);
		
		int lenZ = in.getStackSize();
		for(int z=1;z<=lenZ;z++){
			ImageProcessor byteProcessor = inImageStack.getProcessor(z);
			byteProcessor = getRegionLinePoint(byteProcessor, in.getDimensions());
			inImageStack.setProcessor(byteProcessor, z);
		}
		in.setStack(inImageStack);
		return in;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		OpenDialog o = new OpenDialog("oprn");
		ImagePlus in = new ImagePlus(o.getPath());
		in.show();
		int[] dem={4,4,4};
		in = getRegionLineStack(in,dem);
	}
}
