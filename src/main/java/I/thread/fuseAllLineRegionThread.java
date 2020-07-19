package I.thread;

import ij.process.ByteProcessor;

import java.util.concurrent.CountDownLatch;

public class fuseAllLineRegionThread extends Thread{
	public ByteProcessor outStack;
	public ByteProcessor judgeStack;
	public CountDownLatch countDownLatch;
	public int lenX;
	public int lenY;
	
	@Override
    public void run() {
		for(int x=0;x<lenX;x++){
			for(int y=0;y<lenY;y++){
				//System.out.println("x:"+x+"y:"+y+"z:"+z);
				if(judgeStack.get(x, y)>0){
					outStack.set(x, y, 255);
				}
			}
		}
		countDownLatch.countDown();
	}
	
	public fuseAllLineRegionThread(ByteProcessor outStack,ByteProcessor judgeStack,int lenX,int lenY,CountDownLatch countDownLatch){
		this.outStack = outStack;
		this.judgeStack = judgeStack;
		this.lenX = lenX;
		this.lenY = lenY;
		this.countDownLatch = countDownLatch;
	}
}
