package I.cellcounting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import I.utils.ExcelTools;

public class cellCountingByRegion {

	public static void main(String[] args) throws Exception{
		String url = "E:\\wxc\\level.xls";
		String nameUrl = "E:\\brainmapCompare\\test\\temp_delete\\cache\\rgb_name.xls";
		String[][] temp = ExcelTools.getExcelTools().getData(new File(url), 2);
		String[][] nameTemp = ExcelTools.getExcelTools().getData(new File(nameUrl), 0);
 		Map<Integer, List<Integer>> treeBackMap = new HashMap<>();
		Map<Integer, List<Integer>> treeBeforeMap = new HashMap<>();
		Map<Integer, String> int2NameMap = new HashMap<>();
		
		for(String[] str:nameTemp) {
			int2NameMap.put(Integer.valueOf(str[0]), str[1]);
		}
		
		for(String[] str:temp){
			int val = Integer.valueOf(str[1]);
			if(!int2NameMap.containsKey(val)) int2NameMap.put(val, str[3]);
			String[] beforeStr = str[6].split("/");
			List<Integer> tempList = new ArrayList<>();
			for(int i=0;i<beforeStr.length-1;i++){
				if(!beforeStr[i].equals("")) tempList.add(Integer.valueOf(beforeStr[i]));
			}
			treeBeforeMap.put(val, tempList);
		}
		for(String[] str:temp){
			int depth = Integer.valueOf(str[5]),val = Integer.valueOf(str[1]);
			if(depth==0&&!treeBackMap.containsKey(str[3])){
				treeBackMap.put(val, new ArrayList<>());continue;
			}
			int parentVal = Integer.valueOf(str[4]);
			if(!treeBackMap.containsKey(parentVal)){
				treeBackMap.put(parentVal, new ArrayList<>());
			}
			treeBackMap.get(parentVal).add(val);
		}
		
		
		
	}

}
