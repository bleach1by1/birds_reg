package I.thread;

import java.util.concurrent.CountDownLatch;

import I.utils.LineObtainRegionTools;
import ij.ImagePlus;

public class readRegionThread extends Thread{

	public ImagePlus[] regionList;
	public ImagePlus[] lineList;
	public String url;
	public int location;
	public CountDownLatch countDownLatch;
	
	@Override
    public void run() {
		regionList[location] = new ImagePlus(url);
		lineList[location] = LineObtainRegionTools.getRegionLineStack(new ImagePlus(url), regionList[location].getDimensions());
		countDownLatch.countDown();
	}
	public readRegionThread(ImagePlus[] regionList,ImagePlus[] lineList,String url,int location,CountDownLatch countDownLatch){
		this.regionList = regionList;
		this.lineList = lineList;
		this.location = location;
		this.url = url;
		this.countDownLatch = countDownLatch;
	}
}
