package I.utils;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.OpenDialog;
import ij.process.ImageProcessor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CutOverlapTools {

	public ImageProcessor[] imageList;
	public ThreadPoolExecutor executor;
	
	public CutOverlapTools(ImageProcessor[] imageList){
		this.imageList = imageList;
	}
	
	public void init(int corePoolSize,BlockingQueue<Runnable> link){
		executor = new ThreadPoolExecutor(corePoolSize, corePoolSize+5, 10, TimeUnit.MINUTES, link);
		
	}
	
    public static String[] dp;
    public static Map<String, Integer> map;
    public static boolean getRoot(String input,String judge){
    	while(!dp[map.get(input)].equals(judge)&&!input.equals(dp[map.get(input)])){
    		if(input.equals(judge)) return input.equals(judge);
    		input = dp[map.get(input)];
    	}
    	return dp[map.get(input)].equals(judge);
    }
    
	public static void main(String[] args) throws Exception{
		OpenDialog openDialog = new OpenDialog("");
		String[][] get = ExcelTools.getExcelTools().getData(new File(openDialog.getPath()), 1);
		int len = get.length;
		map = new HashMap<String, Integer>();
		dp = new String[get.length];
		dp[0] = get[0][2];map.put(dp[0], 0);
		for(int i=1;i<len;i++){
			dp[i] = get[i][8];
			map.put(get[i][2], i);
		}
		
		new ImageJ();
		String path = "D:\\data_mask_rcnn\\atlas\\annotation_10.tif";
		ImagePlus imagePlus = new ImagePlus(path);
		ImageStack imageStack = imagePlus.getImageStack();
		int width = imagePlus.getDimensions()[0],height = imagePlus.getDimensions()[1],slices = imagePlus.getImageStackSize();
		ImagePlus show = NewImage.createByteImage("show", width, height, slices, NewImage.FILL_BLACK);
		ImageStack showStack = show.getImageStack();
		OpenDialog op = new OpenDialog("");
		String[][] list = ExcelTools.getExcelTools().getData(new File(op.getPath()), 1);

		HashMap<Integer, Integer> map = new HashMap<>();
		
		for(int i=0;i<list.length;i++){
			for(int j=1;j<len;j++){
				if(getRoot(get[j][2], list[i][0])){
					map.put(Integer.parseInt(get[j][0]), Integer.parseInt(list[i][1]));
				}
			}
		}
		for(int z=0;z<slices;z++){
			for(int y=0;y<height;y++){
				for(int x=0;x<width;x++){
					int val = (int)imageStack.getVoxel(x, y, z);
					//if(set.contains((int)imageStack.getVoxel(x, y, z))) showStack.setVoxel(x, y, z, 250);
					if(map.containsKey(val)) {
						showStack.setVoxel(x, y, z, map.get(val));
					}
				}
			}
		}

		show.setStack(showStack);
		show.show();
	}

}
