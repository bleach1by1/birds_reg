package I.visualization.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import I.plugin.LogIn;
import I.utils.ExcelTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;

public class Cal32bitTo16bit {

	public static ImagePlus calAnnotation32bitTo16bit(String excelUrl,String convertExcelUrl,ImagePlus input) {
		try {
			//ImagePlus input = new ImagePlus(imageUrl);
			String[][] excelStr = ExcelTools.getExcelTools().getData(new File(excelUrl), 0);
			String[][] converExcelStr = ExcelTools.getExcelTools().getData(new File(convertExcelUrl), 0);
			Map<Integer, Integer> convertMap = new HashMap<>();
			for(int i=0;i<excelStr.length;i++) {
				if(!excelStr[i][0].equals(converExcelStr[i][0])) convertMap.put(Integer.valueOf(excelStr[i][0]), Integer.valueOf(converExcelStr[i][0]));
			}
			
			
			int lenX = input.getWidth(), lenY = input.getHeight(), lenZ = input.getStackSize();
			ImagePlus out = NewImage.createShortImage("out", lenX, lenY, lenZ, NewImage.FILL_BLACK);
			ImageStack inputStack = input.getImageStack(),outStack = out.getImageStack();
			for(int z=0;z<lenZ;z++) {
				for(int y=0;y<lenY;y++) {
					for(int x=0;x<lenX;x++) {
						int val = (int)inputStack.getVoxel(x, y, z);
						if(val<=65535) {
							outStack.setVoxel(x, y, z, val);
						}else {
							outStack.setVoxel(x, y, z, convertMap.get(val));
						}
					}
				}
			}
			out.setStack(outStack);
			//new FileSaver(out).saveAsTiff(outUrl);
			return out;
		} catch (Exception e) {
			LogIn.error(e.getMessage());
		}
		return null;
	}
	
	public static void calExcel32bitTo16bit(File excel, String outUrl) {
		try {
			String[][] excelStr = ExcelTools.getExcelTools().getData(excel, 0);
			boolean[] judge = new boolean[65536];
			Map<Integer, Integer> map = new HashMap<>();
			Set<Integer> location = new HashSet<>();
			
			for(int i=0;i<excelStr.length;i++) {
				int val = Integer.valueOf(excelStr[i][0]);
				if(val<=65535) {
					judge[val] = true;
				}
				else {
					map.put(val, val);
					location.add(i);
				}
			}
			
			int begin = 1;
			for(Map.Entry<Integer, Integer> m:map.entrySet()) {
				while(judge[begin]) begin++;
				map.put(m.getKey(), begin);
				judge[begin] = true;
			}
			
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("test1");
			sheet.setDefaultColumnWidth((short) 18);
			int location_e = 0;
			for(int i=0;i<excelStr.length;i++) {
				//if(!box3DMap.containsKey(m.getKey())) continue;
				HSSFRow row2 = sheet.createRow(location_e++);
				HSSFCell cell0 = row2.createCell(0);
				if(location.contains(i))
					cell0.setCellValue(String.valueOf(map.get(Integer.valueOf(excelStr[i][0]))));
				else
					cell0.setCellValue(excelStr[i][0]);
				
				HSSFCell cell5 = row2.createCell(1);
				cell5.setCellValue(excelStr[i][1]);
				HSSFCell cell6 = row2.createCell(2);
				cell6.setCellValue(excelStr[i][2]);
				HSSFCell cell7 = row2.createCell(3);
				cell7.setCellValue(excelStr[i][3]);
				HSSFCell cell8 = row2.createCell(4);
				cell8.setCellValue(excelStr[i][4]);
			}
			
			OutputStream outputStream = new FileOutputStream(outUrl);
			wb.write(outputStream);
			outputStream.close();
			wb.close();
			JOptionPane.showMessageDialog(null, "finish!", "form",JOptionPane.PLAIN_MESSAGE);
		} catch (Exception e) {
			LogIn.error(e.getMessage());
		}
		
	}

}
