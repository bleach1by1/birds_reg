package I.quantitative;

import java.io.File;

import I.utils.ExcelTools;

public class caculDistance {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String excel1Url = "E:\\figure2\\down10\\gd.xls",
				excel2Url = "E:\\figure2\\down10\\pc2.xls";
		String[][] excel1Str = ExcelTools.getExcelTools().getData(new File(excel1Url), 2);
		String[][] excel2Str = ExcelTools.getExcelTools().getData(new File(excel2Url), 2);
		
		for(int i=0,len=excel1Str.length;i<len;i++){
			int x0 = Integer.parseInt(excel1Str[i][0]),
				y0 = Integer.parseInt(excel1Str[i][1]),
				z0 = Integer.parseInt(excel1Str[i][2]),
				x1 = Integer.parseInt(excel2Str[i][0]),
				y1 = Integer.parseInt(excel2Str[i][1]),
				z1 = Integer.parseInt(excel2Str[i][2]);
			int temp = (x0-x1)*(x0-x1)+(y0-y1)*(y0-y1)+(z0-z1)*(z0-z1);
			double distance = Math.sqrt(temp);
			System.out.println(distance);
		}
	}

}
