package I.thread;

import java.util.concurrent.CountDownLatch;

import I.utils.ShellTools;

public class regionTransformThread extends Thread{

	public String regionUrl;
	public String elastixUrl;
	public String paramUrl;
	public String fixName;
	public String outputName;
	public CountDownLatch countDownLatch;
	
	@Override
	public void run(){
		ShellTools.transformUse(regionUrl, elastixUrl, paramUrl, fixName, outputName);
		countDownLatch.countDown();
	}
	
	public regionTransformThread(String regionUrl,String elastixUrl,String paramUrl,String fixName,String outputName,CountDownLatch countDownLatch){
		this.regionUrl = regionUrl;
		this.elastixUrl = elastixUrl;
		this.paramUrl = paramUrl;
		this.fixName = fixName;
		this.outputName = outputName;
		this.countDownLatch = countDownLatch;
	}
}
