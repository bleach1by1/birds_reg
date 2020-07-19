package I.cellcounting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import I.utils.ExcelTools;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;

public class fuseExcelCounting {

	
	public static boolean isNumeric(String str) {
        try {
        	String bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String excelUrl = "E:\\cell_countingall";
		String outUrl = "E:\\figure3\\285_0518\\cell_counting\\all_cell.xls";
		ImagePlus temp = NewImage.createByteImage("out", 1198, 754, 1075, NewImage.FILL_BLACK);
		ImageStack tempStack = temp.getImageStack();
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("test1");
		sheet.setDefaultColumnWidth((short) 18);
		int location = 0;
		
		for(File f:new File(excelUrl).listFiles()) {
			String[][] excelStr = ExcelTools.getExcelTools().getData(f, 0);
			for(String[] str:excelStr) {
				if(!isNumeric(str[0])) continue;
				int x=Integer.valueOf(str[0]),y=Integer.valueOf(str[1]),z=Integer.valueOf(str[2]);
				HSSFRow row2 = sheet.createRow(location++);
				HSSFCell cell0 = row2.createCell(0);
				cell0.setCellValue(x);
				HSSFCell cell5 = row2.createCell(1);
				cell5.setCellValue(y);
				HSSFCell cell6 = row2.createCell(2);
				cell6.setCellValue(z);
				tempStack.setVoxel(x, y, z, 255);
			}
		}
		temp.setStack(tempStack);
		temp.show();
		
		try {
			OutputStream outputStream = new FileOutputStream(outUrl);
			wb.write(outputStream);
			outputStream.close();
			wb.close();
			JOptionPane.showMessageDialog(null, "finish!", "form",JOptionPane.PLAIN_MESSAGE);
		} catch (Exception e2) {}
	}

}
