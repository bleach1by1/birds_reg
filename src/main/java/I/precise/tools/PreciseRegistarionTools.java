package I.precise.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import I.plugin.LogIn;

public class PreciseRegistarionTools {

	
	public static void deformation(String cmd) {
		
		try {
		    Process ps = Runtime.getRuntime().exec(cmd);
		 //   ps.waitFor();
	
			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				LogIn.append(line);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void shell(String cmd) {
		
		try {
		    Process ps = Runtime.getRuntime().exec(cmd);
		 //   ps.waitFor();
	
			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
