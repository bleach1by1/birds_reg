package I.thread;

import ij.ImageStack;

import java.util.concurrent.CountDownLatch;

public class fuseAllRegionThread extends Thread{
	
	public ImageStack outStack;
	public ImageStack judgeStack;
	public CountDownLatch countDownLatch;
	public int lenX;
	public int lenY;
	public int lenZ;
	
	@Override
    public void run() {
		for(int x=0;x<lenX;x++){
			for(int y=0;y<lenY;y++){
				for(int z=0;z<lenZ;z++){
					//System.out.println("x:"+x+"y:"+y+"z:"+z);
					if(judgeStack.getVoxel(x, y, z)>0){
						outStack.setVoxel(x, y, z, 255);
					}
				}
			}
		}
		countDownLatch.countDown();
	}
	
	public fuseAllRegionThread(ImageStack outStack,ImageStack judgeStack,int lenX,int lenY,int lenZ,CountDownLatch countDownLatch){
		this.outStack = outStack;
		this.judgeStack = judgeStack;
		this.lenX = lenX;
		this.lenY = lenY;
		this.lenZ = lenZ;
		this.countDownLatch = countDownLatch;
	}
}
