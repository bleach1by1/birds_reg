package I.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeTools {
	
	private static volatile TimeTools timeTools = null;
	private static SimpleDateFormat df; 
	
	private TimeTools() {}
	
	public static TimeTools getTimeTools() {
		if(timeTools==null) {
			synchronized (TimeTools.class) {
				if(timeTools==null) {
					timeTools = new TimeTools();
					df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				}
			}
		}
		return timeTools;
	}
	
	public String getCurrentTime() {
		return "\t"+df.format(new Date());
	}
	
}
