package I.thread;

import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

public class elastixRegionThread extends Thread{

	public String moveName;
	public ImagePlus moveImage;
	public String pointAndImageUrl;
	public String elastixUrl;
	public String fileNameUrl;
	public CountDownLatch countDownLatch;
	
	@Override
	public void run(){
		saveImage(moveImage, moveName, pointAndImageUrl);
		pointLine(elastixUrl, pointAndImageUrl, fileNameUrl);
		countDownLatch.countDown();
	}
	
	public boolean saveImage(ImagePlus moveImage,String fileNameUrl, String pointAndImageUrl){
		boolean result = true;
		try {
			FileSaver moveSaver = new FileSaver(moveImage);
			moveSaver.saveAsTiff(pointAndImageUrl+"\\"+fileNameUrl);
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(null, "error!");
			return false;
		}
		return result;
	}
	
	public void pointLine(String elastixUrl,String pointAndImageUrl,String fileNameUrl){
		
		String cmd=elastixUrl+"/elastix.exe"+" "+"-f"+" "+pointAndImageUrl+"/fix.tif"+" "+"-m"+" "
					+pointAndImageUrl+"//"+fileNameUrl+".tif"+" "+" "+"-fp"+" "+pointAndImageUrl+
					"/fixPoint.txt"+" "+"-mp"+" "+pointAndImageUrl+"/movePoint.txt"+
					" "+"-out"+" "+pointAndImageUrl+"//"+fileNameUrl+" "+"-p"+" "+pointAndImageUrl+
					"/para-Standard3D-withPoints.txt";
		try {
		    Process ps = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			String result = sb.toString();
			System.out.println(result);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public elastixRegionThread(String moveName,ImagePlus moveImage,String pointAndImageUrl,String elastixUrl,String fileNameUrl,CountDownLatch countDownLatch){
		this.elastixUrl = elastixUrl;
		this.moveName = moveName;
		this.moveImage = moveImage;
		this.pointAndImageUrl = pointAndImageUrl;
		this.fileNameUrl = fileNameUrl;
		this.countDownLatch = countDownLatch;
	}
}
