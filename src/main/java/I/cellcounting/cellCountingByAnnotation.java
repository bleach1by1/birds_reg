package I.cellcounting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.bitplane.xt.BPImarisLib;

import I.utils.ExcelTools;
import I.visualization.tools.ImarisTools;
import ij.ImagePlus;
import ij.ImageStack;

public class cellCountingByAnnotation {

	public static void main(String[] args) throws Exception{
		String imageUrl = "E:\\figure3\\285_0518\\cell_counting\\annotation.tif";
		String excelUrl = "E:\\brainmapCompare\\test\\temp_delete\\cache\\rgb_name.xls";
		String[][] excelStr = ExcelTools.getExcelTools().getData(new File(excelUrl), 0);
		Map<String, Integer> numMap = new HashMap<>();
		Map<Integer, String> int2NameMap = new HashMap<>();
		for(String[] str:excelStr) {
			int2NameMap.put(Integer.valueOf(str[0]), str[1]);
		}
		
		ImagePlus image = new ImagePlus(imageUrl);
		ImageStack imageStack = image.getImageStack();
		
		int lenX = image.getWidth(), lenY = image.getHeight(), lenZ = image.getStackSize();
		
		
		BPImarisLib vImarisLib = new BPImarisLib();
		int imarisId = ImarisTools.GetObjectId(vImarisLib);
		System.out.println(imarisId);
		Imaris.IApplicationPrx application = vImarisLib.GetApplication(imarisId);
		Imaris.IDataSetPrx  dataSet = application.GetDataSet();
		Imaris.ISpotsPrx allSpots = application.GetFactory().ToSpots(application.GetSurpassSelection());
		float[][] spotsLocation = allSpots.GetPositionsXYZ();
		
		for(float[] f:spotsLocation) {
			int x = (int)f[0]/20,y = (int)f[1]/20,z = (int)f[2]/20;
			int val = (int)imageStack.getVoxel(x, y, z);
			if(val==0) continue;
			String name = int2NameMap.get(val);
			if(!numMap.containsKey(name)) numMap.put(name, 0);
			numMap.put(name, numMap.get(name)+1);
		}
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("test1");
		sheet.setDefaultColumnWidth((short) 18);
		int location = 0;
		
		for(Map.Entry<String, Integer> m:numMap.entrySet()) {
			HSSFRow row2 = sheet.createRow(location++);
			HSSFCell cell2 = row2.createCell(0);
			cell2.setCellValue(m.getKey());
			HSSFCell cell6 = row2.createCell(1);
			cell6.setCellValue(m.getValue());
		}
		
		String outUrl = "E:\\figure3\\285_0518\\cell_counting\\region\\gobal\\all_cell_num.xls";
		
		OutputStream outputStream = new FileOutputStream(outUrl);
		wb.write(outputStream);
		outputStream.close();
		wb.close();
	}

}
