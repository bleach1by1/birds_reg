package I.deeplearning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import I.utils.ExcelTools;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;

public class fuseSomeDemo implements Runnable{

	public static int lenX,lenY,lenZ;
	
	public ImageStack imageStack;
	public Set<Integer> tempType;
	public ImageStack outStack;
	public int z;
	public int valTemp;
	public CountDownLatch count;
	
	public fuseSomeDemo(ImageStack imageStack,Set<Integer> tempType, ImageStack outStack,int z,int valTemp, CountDownLatch count){
		this.imageStack = imageStack;
		this.tempType = tempType;
		this.outStack = outStack;
		this.z = z;
		this.valTemp = valTemp;
		this.count = count;
	}
	
	@Override
	public void run() {
		for(int y=0;y<lenY;y++) {
			for(int x=0;x<lenX;x++) {
				int val = (int)imageStack.getVoxel(x, y, z);
				if(tempType.contains(val)) {
					outStack.setVoxel(x, y, z, valTemp);
				}
			}
		}
		count.countDown();
	}
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		new ImageJ();
		String chooseLevelUrl = "G:\\annotation\\level_rgb_name.xls";
		String chooseRegionUrl = "G:\\annotation\\test_rgb.xls";
		String url = "I:\\han\\YH285\\result.tif";
		String outUrl = "I:\\han\\dl\\hpf\\YH212.tif";
		
		String[][] excelLevelStr = ExcelTools.getExcelTools().getData(new File(chooseLevelUrl), 1);
		String[][] excelRegionStr = ExcelTools.getExcelTools().getData(new File(chooseRegionUrl), 0);
		ImagePlus image = new ImagePlus(url);
		ImageStack imageStack = image.getImageStack();
		Set<Integer> set = new HashSet<>();
		for(String[] str:excelRegionStr) {
			set.add(Integer.parseInt(str[0])); 
		}
		
		String judgeUrl = "I:\\han\\quicknat\\4roi\\region_4.xls";
		String[][] judgeStr = ExcelTools.getExcelTools().getData(new File(judgeUrl), 0);
		Set<String> judgeSet = new HashSet<>();
		Map<String, Integer> nameValMap = new HashMap<>();
		int valName = 1;
		for(String[] j:judgeStr){
			judgeSet.add(j[0]);
			nameValMap.put(j[0], valName++);
		}
		
		List<Integer> typeList = new ArrayList<>();
		int num = 0;
		for(String[] str:excelLevelStr){
			if(judgeSet.contains(str[4])){
				//String[] tempList = str[13].substring(1, str[13].length()-1).split("/");
				//if(Integer.parseInt(tempList[2])==567) typeList.add(num);
				typeList.add(num);
			}
			num++;
		}
		
		lenX = image.getWidth(); lenY = image.getHeight(); lenZ = image.getStackSize();
		ImagePlus out = NewImage.createByteImage("out", lenX, lenY, lenZ, NewImage.FILL_BLACK);
		ImageStack outStack = out.getImageStack();
		for(Integer i:typeList){
			int level = Integer.parseInt(excelLevelStr[i][10]);
			int type = Integer.parseInt(excelLevelStr[i][0]);
			Map<Integer, Integer> temp = new HashMap<>();
			Set<Integer> overLapSet = new HashSet<>();
			
			for(String[] str:excelLevelStr) {
				if(str[10].length()>0&&Integer.parseInt(str[10])==level) {
					overLapSet.add(Integer.parseInt(str[0]));
				}
			}
			
			for(String[] str:excelLevelStr) {
				int val = Integer.parseInt(str[0]);
				if(set.contains(val)) {
					System.out.println(val);
					String[] list = str[13].substring(1, str[13].length()-1).split("/");
					if(list.length<=level) {
						System.out.println("error: "+str[0]);continue;
					}else {
						int val3 = Integer.valueOf(list[level]);
						if(!overLapSet.contains(val3)) {
							System.out.println("error: "+str[0]);continue;
						}else {
							temp.put(Integer.parseInt(str[0]), val3);
						}
					}
				}
			}
			
			Set<Integer> tempType = new HashSet<>();
			for(Map.Entry<Integer, Integer> m:temp.entrySet()) {
				if(m.getValue()==type) {
					tempType.add(m.getKey());
				}
			}
			
			int valTemp = nameValMap.get(excelLevelStr[i][4]);
			/*
			for(int x=0;x<lenX;x++) {
				for(int y=0;y<lenY;y++) {
					for(int z=0;z<lenZ;z++) {
						int val = (int)imageStack.getVoxel(x, y, z);
						if(tempType.contains(val)) {
							outStack.setVoxel(x, y, z, valTemp);
						}
					}
				}
			}
			*/
			CountDownLatch count = new CountDownLatch(lenZ);
			for(int z=0;z<lenZ;z++){
				new Thread(new fuseSomeDemo(imageStack, tempType, outStack, z, valTemp, count)).start();
			}
			try {
				count.await();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		out.setStack(outStack);
		
		
		
	//	ImagePlus outReslice = NewImage.createByteImage("", lenZ, lenY, lenX, NewImage.FILL_BLACK);
	//	ImageStack outResliceStack = outReslice.getImageStack();
		/*
		for(int x=0;x<lenX;x++) {
			for(int y=0;y<lenY;y++) {
				for(int z=0;z<lenZ;z++) {
					outResliceStack.setVoxel(z, y, x, outStack.getVoxel(x, y, z));
				}
			}
		}
		outReslice.setStack(outResliceStack);
		outReslice.show();
		*/
		out.show();
		FileSaver save = new FileSaver(out);
		save.saveAsTiff(outUrl);
	}



}
