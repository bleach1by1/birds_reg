package I.precise.thread;

import java.util.concurrent.CountDownLatch;

import ij.process.ImageProcessor;

public class SavePixelValueThread implements Runnable{

	private ImageProcessor inProcessor;
	private float[][] annotationFloat;
	private int val;
	private CountDownLatch count;
	
	@Override
	public void run() {
		int lenX = inProcessor.getWidth(), lenY = inProcessor.getHeight();
		for(int x=0;x<lenX;x++) {
			for(int y=0;y<lenY;y++) {
				if(inProcessor.getPixelValue(x, y)>0) annotationFloat[x][y] = val;
			}
		}
		count.countDown();
	}

	public SavePixelValueThread(ImageProcessor inProcessor, float[][] annotationFloat, int val, CountDownLatch count) {
		this.inProcessor = inProcessor;
		this.annotationFloat = annotationFloat;
		this.val = val;
		this.count = count;
	}
	
}
