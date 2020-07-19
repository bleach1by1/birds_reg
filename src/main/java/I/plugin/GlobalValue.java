package I.plugin;

import ij.ImagePlus;

public class GlobalValue {
	
	public final static String CACHEURL = "https://github.com/bleach1by1/cache/archive/master.zip";
	
	public final static int MAXTHREAD = Runtime.getRuntime().availableProcessors();

	public final static int ALLENLENX = 570, ALLENLENY = 400, ALLENLENZ = 660;
	
	public final static double REGVOXEL = 20.0;	//um
	
	public static String URL = null;
	
	public static boolean INTERACTION = false;
	
	public static ImagePlus downSampleImage = null;
}
