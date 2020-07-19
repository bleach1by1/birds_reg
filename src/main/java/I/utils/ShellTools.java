package I.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ShellTools {

	public static void transformUse(String regionUrl,String elastixUrl,String paramUrl,String fixName,String outputName){
		String cmd = elastixUrl+"/transformix.exe"+" -in "+regionUrl+"\\"+fixName+
					" -out "+paramUrl+"//"+outputName+" -tp "+paramUrl+"\\rigid_affine_bspline_out\\TransformParameters.2.txt";
		
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
	
	public void transform(String elastixUrl,String paramUrl,String fixName,String moveName){
		File targetFile = new File(paramUrl+"//rigid_affine_bspline_out");
		targetFile.mkdirs();
		String cmd = elastixUrl+"/elastix.exe"+" "+ " -f "+paramUrl+"\\"+fixName+".tif "+
					" -m "+paramUrl+"\\"+moveName+".tif"+" -out "+paramUrl+"\\rigid_affine_bspline_out"+" -p "
					+paramUrl+"\\para-Standard_rigid.txt"+" -p "+paramUrl+"\\para-Standard_affine.txt"+
					" -p "+paramUrl+"\\para-Standard_bspline.txt";
		try {
		    Process ps = Runtime.getRuntime().exec(cmd);
		 //   ps.waitFor();
 
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
	
	public void pointLine(String elastixUrl,String pointAndImageUrl){
		
		String cmd=elastixUrl+"/elastix.exe"+" "+"-f"+" "+pointAndImageUrl+"/fix.tif"+" "+"-m"+" "
					+pointAndImageUrl+"/move.tif"+" "+" "+"-fp"+" "+pointAndImageUrl+
					"/fixPoint.txt"+" "+"-mp"+" "+pointAndImageUrl+"/movePoint.txt"+
					" "+"-out"+" "+pointAndImageUrl+"/out"+" "+"-p"+" "+pointAndImageUrl+
					"/para-Standard3D-withPoints.txt";
		try {
		    Process ps = Runtime.getRuntime().exec(cmd);
		 //   ps.waitFor();
 
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
