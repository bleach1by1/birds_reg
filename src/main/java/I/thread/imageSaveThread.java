package I.thread;

import java.util.concurrent.CountDownLatch;

import ij.ImagePlus;
import ij.io.FileSaver;

public class imageSaveThread extends Thread{

	public String url;
	public ImagePlus out;
	public CountDownLatch countDownLatch;
	
	@Override
	public void run(){
		FileSaver fixSaver = new FileSaver(out);
		fixSaver.saveAsTiff(url);
		countDownLatch.countDown();
	}
	
	public imageSaveThread(String url,ImagePlus out,CountDownLatch countDownLatch){
		this.url = url;
		this.out = out;
		this.countDownLatch = countDownLatch;
	}
}
