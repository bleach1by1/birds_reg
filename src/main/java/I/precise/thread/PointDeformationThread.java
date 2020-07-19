package I.precise.thread;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import I.utils.TimeTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

public class PointDeformationThread implements Runnable{

	private Map<Integer, String> valNameMap;
	private String root;
	private Rectangle r;
	private int val;
	private ImageProcessor imageProcessor;
	private CountDownLatch countDownLatch;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		new File(root+"\\"+valNameMap.get(val)+"\\outSlice").mkdirs();
		savePointAsText();
		transformShell(root, root+"\\"+valNameMap.get(val), root+"\\"+valNameMap.get(val)+"\\outSlice");
		countDownLatch.countDown();
	}
	
	public PointDeformationThread(Map<Integer, String> valNameMap, String root, Rectangle r,
			int val, ImageProcessor imageProcessor, CountDownLatch countDownLatch) {
		this.valNameMap = valNameMap;
		this.root = root;
		this.r = r;
		this.val = val;
		this.imageProcessor = imageProcessor;
		this.countDownLatch = countDownLatch;
	}
	
	private boolean savePointAsText() {
		boolean result = true;
		try {
			ImagePlus moveImage = NewImage.createByteImage("moveImage", imageProcessor.getWidth(),
					imageProcessor.getHeight(), 1, NewImage.FILL_BLACK);
			ImageStack moveImageStack = moveImage.getImageStack();
			ImageProcessor tempProcessor = moveImageStack.getProcessor(1);
			for(int x=r.x;x<=r.x+r.width&&x<imageProcessor.getWidth();x++) {
				for(int y=r.y;y<=r.y+r.height&&y<imageProcessor.getHeight();y++) {
					int temp = (int)imageProcessor.getPixelValue(x, y);
					if(temp==val) {
						tempProcessor.set(x, y, 255);
					}
				}
			}
			moveImage.setStack(moveImageStack);

			new FileSaver(moveImage).saveAsTiff(root+"\\"+valNameMap.get(val)+"\\move.tif");
			System.out.println(root+"\\"+valNameMap.get(val)+"\\moveImage.tif"+"  "+TimeTools.getTimeTools().getCurrentTime());
		} catch (Exception e2) {
		}
		return result;
	}
	
	public void transformShell(String root,String moveUrl,String outUrl){
		
		String cmd=root+"/elastix.exe"+" "+"-f"+" "+root+"/fix.tif"+" "+"-m"+" "
					+moveUrl+"//move.tif"+" "+" "+"-fp"+" "+root+
					"/fixPoint.txt"+" "+"-mp"+" "+root+"/movePoint.txt"+
					" "+"-out"+" "+outUrl+" "+"-p"+" "+root+
					"/para-Standard3D-withPoints_gpu.txt";
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

	
}
