package I.plugin;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import I.coarse.entity.InvertLineEntity;
import I.coarse.tools.InvertTools;
import I.utils.FileCopyUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.io.FileSaver;

public class AdditionalFile {

	/**
	 * Save the required files in the cache
	 * @param inUrl file's url
	 * @param outUrl cache's url
	 * @return true:successful operation;false:operation failed
	 */
	public static boolean saveFile(String inUrl,String outUrl){
		boolean out = false;
		try {
			if(!new File(outUrl+"/tempOrgImage.tif").exists()){
				File temp = new File(inUrl+"/tempOrgImage.tif");
				FileCopyUtils.copyFileUsingFileChannels(temp, new File(outUrl+"/tempOrgImage.tif"));
				temp.delete();
			}
			
			if(!new File(outUrl+"/annotationOrgImage.tif").exists()){
				File temp = new File(inUrl+"/annotationOrgImage.tif");
				FileCopyUtils.copyFileUsingFileChannels(temp, new File(outUrl+"/annotationOrgImage.tif"));
				temp.delete();
			}
			
			if(!new File(outUrl+"/Atlas132label.tif").exists()){
				File temp = new File(inUrl+"/Atlas132label.tif");
				FileCopyUtils.copyFileUsingFileChannels(temp, new File(outUrl+"/Atlas132label.tif"));
				temp.delete();
			}
			if(!new File(outUrl+"/orgInvertImage.tif").exists()) {
				ImagePlus tempImage = new ImagePlus(outUrl+"/tempOrgImage.tif");
				int[] size = new int[3];
				size[0] = tempImage.getWidth();size[1] = tempImage.getHeight();size[2] = tempImage.getStackSize();
				ImagePlus outImage = NewImage.createByteImage("orgInvertImage", size[0], size[1], size[2], NewImage.FILL_BLACK);
				ImageStack tempStack = tempImage.getImageStack(),outStack = outImage.getImageStack();
				List<InvertLineEntity> tempList = new ArrayList<>();
				InvertLineEntity yz1 = new InvertLineEntity();InvertLineEntity yz2 = new InvertLineEntity();
				InvertLineEntity yz3 = new InvertLineEntity();InvertLineEntity yz4 = new InvertLineEntity();
				InvertLineEntity xz1 = new InvertLineEntity();InvertLineEntity xz2 = new InvertLineEntity();
				InvertLineEntity xy1 = new InvertLineEntity();InvertLineEntity xy2 = new InvertLineEntity();
				yz1.setBegin(new Point(285, 50));yz1.setEnd(new Point(285, 100));yz1.setStackNum(20);yz1.setType("YZ");
				yz2.setBegin(new Point(285, 60));yz2.setEnd(new Point(285, 110));yz2.setStackNum(80);yz2.setType("YZ");
				yz3.setBegin(new Point(285, 50));yz3.setEnd(new Point(285, 100));yz3.setStackNum(90);yz3.setType("YZ");
				yz4.setBegin(new Point(285, 60));yz4.setEnd(new Point(285, 110));yz4.setStackNum(100);yz4.setType("YZ");
				xz1.setBegin(new Point(260, 195));xz1.setEnd(new Point(306, 195));xz1.setStackNum(211);xz1.setType("XZ");
				xz2.setBegin(new Point(267, 238));xz2.setEnd(new Point(302, 238));xz2.setStackNum(599);xz2.setType("XZ");
				xy1.setBegin(new Point(227, 117));xy1.setEnd(new Point(227, 146));xy1.setStackNum(277);xy1.setType("XY");
				xy2.setBegin(new Point(341, 117));xy2.setEnd(new Point(341, 141));xy2.setStackNum(277);xy2.setType("XY");
				tempList.add(yz1);
				tempList.add(yz2);
				tempList.add(yz3);
				tempList.add(yz4);
				tempList.add(xz1);
				tempList.add(xz2);
				tempList.add(xy1);
				tempList.add(xy2);
				double[][][] thumbMask = InvertTools.invert(tempList, size);
				for(int i=0;i<size[2];i++){
			 		for(int j=0;j<size[1];j++){
			 			for(int k=0;k<size[0];k++){
			 				int val = (int)tempStack.getVoxel(k, j, i);
			 				if(val>=10) {
			 					outStack.setVoxel(k, j, i, thumbMask[i][j][k]>0?(255-val):val);
			 				}
			 			}
			 		}
			 	}
				outImage.setStack(outStack);
				new FileSaver(outImage).saveAsTiff(outUrl+"/orgInvertImage.tif");
			}
			out = true;
		} catch (Exception e) {
			IJ.log(e.getMessage());
		}
		
		return out;
	}
	
	
	 public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
	}
}
