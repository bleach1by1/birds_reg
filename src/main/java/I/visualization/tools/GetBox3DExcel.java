package I.visualization.tools;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;

public class GetBox3DExcel {

	public static void getBox3DExcel(ImagePlus input,String outUrl) {
		int lenX = input.getWidth(), lenY = input.getHeight(), lenZ = input.getStackSize();
		ImageStack inputStack = input.getImageStack();
		
		
		Map<Integer, location> box3DMap = new ConcurrentHashMap<>();
		
		Map<Integer, List<Map<Integer, Point>>> tempMap = new ConcurrentHashMap<>();
		
		int maxThread = 50,batch = (lenZ/maxThread)+(((lenZ%maxThread)>0)?1:0);
		for(int b=0;b<batch;b++) {
			int numThread = ((lenZ-b*maxThread)>maxThread)?maxThread:(lenZ-b*maxThread);
			CountDownLatch count = new CountDownLatch(numThread);
			for(int z=b*maxThread;z<b*maxThread+numThread;z++) {
				int zz = z;
				new Thread(()->{
					Map<Integer, Point> minMap = new HashMap<>();
					Map<Integer, Point> maxMap = new HashMap<>();
					for(int y=0;y<lenY;y++) {
						for(int x=0;x<lenX;x++) {
							int val = (int)inputStack.getVoxel(x, y, zz);
							if(val==0) continue;
							if(!minMap.containsKey(val)) {
								Point p1 = new Point(x, y);
								Point p2 = new Point(x, y);
								minMap.put(val, p1);
								maxMap.put(val, p2);
							}
							Point tempMin = minMap.get(val),tempMax = maxMap.get(val);
							tempMin.x = Math.min(tempMin.x, x);
							tempMin.y = Math.min(tempMin.y, y);
							tempMax.x = Math.max(tempMax.x, x);
							tempMax.y = Math.max(tempMax.y, y);
							minMap.put(val, tempMin);
							maxMap.put(val, tempMax);
						}
					}
					List<Map<Integer, Point>> temp = new ArrayList<>();
					temp.add(minMap);
					temp.add(maxMap);
					tempMap.put(zz, temp);
					
					count.countDown();
				}).start();
			}
			try {
				count.await();
			} catch (Exception e) {}
		}
		for(int z=0;z<lenZ;z++) {
			Map<Integer,Point> minMap = tempMap.get(z).get(0);
			Map<Integer,Point> maxMap = tempMap.get(z).get(1);
			for(Map.Entry<Integer, Point> m:minMap.entrySet()) {
				int val = m.getKey();
				Point min = m.getValue(),max = maxMap.get(val);
				if(!box3DMap.containsKey(val)) {
					location temp = new location();
					temp.x0 = min.x;temp.y0 = min.y;temp.z0 = z;
					temp.x1 = max.x;temp.y1 = max.y;temp.z1 = z;
					box3DMap.put(val, temp);
				}
				location tempL = box3DMap.get(val);
				tempL.x0 = Math.min(tempL.x0, min.x);
				tempL.y0 = Math.min(tempL.y0, min.y);
				tempL.z0 = Math.min(tempL.z0, z);
				tempL.x1 = Math.max(tempL.x1, max.x);
				tempL.y1 = Math.max(tempL.y1, max.y);
				tempL.z1 = Math.max(tempL.z1, z);
				box3DMap.put(val, tempL);
			}
		}
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("1");
		sheet.setDefaultColumnWidth((short) 18);
		
		int location = 0;
		for(Map.Entry<Integer, location> m:box3DMap.entrySet()) {
			HSSFRow row2 = sheet.createRow(location++);
			HSSFCell cell0 = row2.createCell(0);
			
			
			location tempLocation = m.getValue();
			cell0.setCellValue(m.getKey());
			
			HSSFCell cell5 = row2.createCell(1);
			cell5.setCellValue(tempLocation.x0);
			HSSFCell cell6 = row2.createCell(2);
			cell6.setCellValue(tempLocation.y0);
			HSSFCell cell7 = row2.createCell(3);
			cell7.setCellValue(tempLocation.z0);
			HSSFCell cell8 = row2.createCell(4);
			cell8.setCellValue(tempLocation.x1);
			HSSFCell cell9 = row2.createCell(5);
			cell9.setCellValue(tempLocation.y1);
			HSSFCell cell10 = row2.createCell(6);
			cell10.setCellValue(tempLocation.z1);
		}
		
		//String outUrl = "E:\\han\\YH285\\tracing\\wholeBrainConvert.xls";
		try {
			OutputStream outputStream = new FileOutputStream(outUrl);
			wb.write(outputStream);
			outputStream.close();
			wb.close();
			JOptionPane.showMessageDialog(null, "finish!", "form",JOptionPane.PLAIN_MESSAGE);
		} catch (Exception e2) {}
	}
	
	public static void main(String[] args) throws Exception{
		new ImageJ();
		String input = "E:\\figure3\\285_0518\\cell_counting\\annotation_convert.tif";
		String output = "E:\\figure3\\285_0518\\cell_counting/wholeBrain.xls";
		ImagePlus inputPlus = new ImagePlus(input);
		GetBox3DExcel.getBox3DExcel(inputPlus, output);
		
	}
	
}

class location{
	int x0;
	int y0;
	int z0;
	int x1;
	int y1;
	int z1;
}