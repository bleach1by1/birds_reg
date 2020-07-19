package I.cellcounting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import I.utils.ExcelTools;
import ij.ImagePlus;
import ij.ImageStack;

public class cellCountingByName {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String cellUrl = "E:\\figure3\\285_0518\\cell_counting\\region\\our\\all_cell.xls";
		String imageUrl = "E:\\figure3\\285_0518\\cell_counting\\annotation_convert.tif";
		String regionUrl = "E:\\figure3\\285_0518\\cell_counting\\region.xls";
		String outUrl = "E:\\figure3\\285_0518\\cell_counting\\region\\temp\\region_out.xls";
		
		ImagePlus input = new ImagePlus(imageUrl);
		int lenX = input.getWidth(), lenY = input.getHeight(), lenZ = input.getStackSize();
		ImageStack inputStack = input.getImageStack();
		
		String[][] excelStr = ExcelTools.getExcelTools().getData(new File(cellUrl), 0);
		Map<Integer, Integer> numMap = new HashMap<>();
		Map<Integer, Integer> voxelMap = new HashMap<>(); 
		for(String[] str:excelStr){
			int x = Integer.valueOf(str[0]),
				y = Integer.valueOf(str[1]),
				z = Integer.valueOf(str[2]);
			if(x>=lenX||y>=lenY||z>=lenZ) continue;
			int val = (int)inputStack.getVoxel(x, y, z);
			if(val==0) continue;
			if(!numMap.containsKey(val)) numMap.put(val, 0);
			numMap.put(val, numMap.get(val)+1);
		} 
		
		Set<Integer> temp = new HashSet<>();
		for(int z=0;z<lenZ;z++) {
			for(int y=0;y<lenY;y++) {
				for(int x=0;x<lenX;x++) {
					int val = (int)inputStack.getVoxel(x, y, z);
					temp.add(val);
					if(val==0) continue;
					if(!voxelMap.containsKey(val)) voxelMap.put(val, 0);
					voxelMap.put(val, voxelMap.get(val)+1);
				}
			}
		}
		
		for(Integer i:temp) {
			System.out.println(i);
		}
		
		String[][] regionStr = ExcelTools.getExcelTools().getData(new File(regionUrl), 0);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("test1");
		sheet.setDefaultColumnWidth((short) 18);
		int location = 0;
		
		for(int i=0;i<regionStr.length;i++){
			
			HSSFRow row2 = sheet.createRow(i);
			HSSFCell cell2 = row2.createCell(0);
			cell2.setCellValue(regionStr[i][0]);
			HSSFCell cell6 = row2.createCell(1);
			if(numMap.containsKey(i+1))
				cell6.setCellValue(numMap.get(i+1));
			else
				cell6.setCellValue(0);
			HSSFCell cell3 = row2.createCell(2);
			if(voxelMap.containsKey(i+1))
				cell3.setCellValue(voxelMap.get(i+1));
		}
			
		OutputStream outputStream = new FileOutputStream(outUrl);
		wb.write(outputStream);
		outputStream.close();
		wb.close();
		
	}

}
