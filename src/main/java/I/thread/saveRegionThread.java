package I.thread;

import java.util.concurrent.CountDownLatch;

import I.utils.MedianTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.process.ImageProcessor;

public class saveRegionThread extends Thread{

	public ImagePlus saveImage;
	public CountDownLatch countDownLatch;
	public ImagePlus[] outArray;
	public int location;
	
	@Override
	public void run(){
		ImageStack saveStack = saveImage.getImageStack();
		outArray[location] = NewImage.createByteImage("", saveImage.getWidth(), saveImage.getHeight(),
							saveImage.getStackSize(), NewImage.FILL_BLACK);
		ImageStack outStack = outArray[location].getImageStack();
		int[] dem = saveImage.getDimensions();
		int radius = 3;
		for(int z=1;z<=saveImage.getStackSize();z++){
			ImageProcessor byteProcessor = saveStack.getProcessor(z);
			for(int x=0;x<byteProcessor.getWidth();x++){
				for(int y=0;y<byteProcessor.getHeight();y++){
					if(byteProcessor.get(x, y)>5){
						byteProcessor.set(x, y, 255);
					}else{
						byteProcessor.set(x,y,0);
					}
				}
			}
			byteProcessor = MedianTools.medianBinary(byteProcessor, dem, radius);
			outStack.setProcessor(byteProcessor, z);
		}
		outArray[location].setStack(outStack);
		countDownLatch.countDown();
	}
	
	public saveRegionThread(ImagePlus[] outArray,int location,ImagePlus saveImage,CountDownLatch countDownLatch){
		this.outArray = outArray;
		this.location = location;
		this.saveImage = saveImage;
		this.countDownLatch = countDownLatch;
	}
}
