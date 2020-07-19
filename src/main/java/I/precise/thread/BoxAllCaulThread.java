package I.precise.thread;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import I.precise.tools.LineTools;
import ij.process.ImageProcessor;

public class BoxAllCaulThread extends Thread{

	
	private Map<Integer, Map<Integer, List<Rectangle>>> boxAllMap;
	private int z;
	private ImageProcessor imageProcessor;
	private CountDownLatch countDownLatch;
	
	public void run() {
		boxAllMap.put(z, LineTools.getLineTool().getBoxAllSlice(imageProcessor));
		countDownLatch.countDown();
	}
	
	public BoxAllCaulThread(Map<Integer, Map<Integer, List<Rectangle>>> boxAllMap, int z, ImageProcessor imageProcessor,CountDownLatch countDownLatch) {
		this.boxAllMap = boxAllMap;
		this.z = z;
		this.imageProcessor = imageProcessor;
		this.countDownLatch = countDownLatch;
		
	}
}
