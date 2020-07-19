package I.coarse.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ParamTools {

	public static void changeCoarse(String url) throws Exception{
		 String pathname = url;
		 InputStreamReader reader = new InputStreamReader(new FileInputStream(pathname));
		 BufferedReader br = new BufferedReader(reader);
		 List<String> in = new ArrayList<>();
		 String line;
		 while ((line=br.readLine())!=null) {
			in.add(line);
		 }
		 br.close();
		 (new File(pathname)).delete();
		 File writename = new File(pathname); 
         writename.createNewFile(); 
		 BufferedWriter out = new BufferedWriter(new FileWriter(pathname)); 
		 for(String s:in) {
			 if(s.indexOf("FinalBSplineInterpolationOrder")>-1&&s.indexOf("ResampleInterpolator")==-1
					 &&s.indexOf("3")>-1) {
				 int temp = s.indexOf("3");
				 StringBuilder str = new StringBuilder(s);
				 str.replace(temp, temp+1, "0");
				 s = str.toString();
			 }
			 if(s.indexOf("ResultImagePixelType")>-1&&s.indexOf("short")>-1) {
				 int temp = s.indexOf("short");
				 StringBuilder str = new StringBuilder(s);
				 str.replace(temp, temp+5, "float");
				 s = str.toString();
			 }
			 out.write(s+"\r\n");
			 out.flush();
		 }
		 out.close();
		 System.out.println("ok");
	}
	
}
