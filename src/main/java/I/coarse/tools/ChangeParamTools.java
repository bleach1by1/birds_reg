package I.coarse.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class ChangeParamTools {

	public static void changeParam(String url,Map<String, String> map){
		StringBuffer bufAll = new StringBuffer(); 
		BufferedReader br = null;
		try{
			String line = null;
			br = new BufferedReader(new FileReader(url));
			while ((line=br.readLine())!=null) {
				for(Map.Entry<String, String> m:map.entrySet()){
					if(line.startsWith(m.getKey())){
						line = m.getKey()+" "+m.getValue()+")";
						break;
					}
				}
				bufAll.append(line+"\n");
			}
			br.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter(url));
			bw.write(bufAll.toString());
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public static void main(String[] args) {
		String url = "E:\\myeclispe\\brainMap\\brainMap\\sourses\\para-Standard_bspline_mutil3.txt";
		Map<String, String> map = new HashMap<>();
		map.put("(Metric0Weight", "0.125");
		map.put("(Metric1Weight", "0.125");
		map.put("(Metric2Weight", "0.125");
		map.put("(MaximumNumberOfIterations", "1000");
		changeParam(url, map);
	}

}
