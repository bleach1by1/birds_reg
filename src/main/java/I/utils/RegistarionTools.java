package I.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import I.plugin.LogIn;

public class RegistarionTools {

	
	public static void deformation(String cmd) {
		
		try {
		    Process ps = Runtime.getRuntime().exec(cmd);
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
			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			while ((br.readLine()) != null) {
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
