package I.utils;

import ij.process.ImageProcessor;

public class MedianTools {

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
