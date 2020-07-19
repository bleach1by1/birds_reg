package I.plugin;

import javax.swing.JTextArea;

import I.utils.TimeTools;

public class LogIn {

	public static void error(String in) {
		String out = "error:   "+in+TimeTools.getTimeTools().getCurrentTime();
		out+="\n";
		BirdsPlugin.SOUTHTEXT.append(out);
		BirdsPlugin.SOUTHTEXT.selectAll();
	}
	
	public static void show(String in) {
		String out = "log:   "+in+TimeTools.getTimeTools().getCurrentTime();
		out+="\n";
		BirdsPlugin.SOUTHTEXT.append(out);
		BirdsPlugin.SOUTHTEXT.selectAll();
	}
	
	public static void append(String in) {
		BirdsPlugin.SOUTHTEXT.append(in+"\n");
		BirdsPlugin.SOUTHTEXT.selectAll();
	}
	
	public static void schedule(String all,String section,String title) {
		JTextArea temp = BirdsPlugin.SOUTHTEXT;
		//BirdsPlugin.SOUTHTEXT.append("download:  "+section+"/"+all);
		try {
			int count = temp.getLineCount();
			int start = temp.getLineStartOffset(count-1),end = temp.getLineEndOffset(count-1);
			temp.replaceRange(title+section+"/"+all, start, end);
			temp.selectAll();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
