package I.visualization.tools;

import I.plugin.GlobalValue;
import I.plugin.LogIn;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;

public class UpSampleTools {

	public static ImagePlus zRestoreShort1(String brainNum,String altas,ImagePlus in,int upZ){
		int lenX = in.getWidth(), lenY = in.getHeight(),lenZ = in.getStackSize();
		
		ImagePlus annotationOut = NewImage.createImage("annotationOut", lenX, lenY, upZ,in.getBitDepth(), NewImage.FILL_BLACK);
		ImageStack annotationOutStack = annotationOut.getImageStack(),inStack = in.getImageStack();
		
		String[] brainNumStrList = brainNum.split(",");
		String[] altasStrList = altas.split(",");
		int lenNum = altasStrList.length;
		
		int[] brainNumList = new int[lenNum];
		int[] altasList = new int[lenNum];
		if(lenNum<2) {
			brainNumList = new int[] {1,upZ};
			altasList = new int[] {1,GlobalValue.ALLENLENZ};
		}else {
			for(int i=0;i<lenNum;i++) {
				brainNumList[i] = Integer.valueOf(brainNumStrList[i]);
				altasList[i] = Integer.valueOf(altasStrList[i]);
			}
		}
		
		int beginB =brainNumList[0],beginA = altasList[0],
			endB = brainNumList[brainNumList.length-1],endA = altasList[altasList.length-1];
		double lenBZ = endB-beginB, lenAZ = endA-beginA, lenCZ = (double)lenZ-((double)upZ-lenBZ)/(double)upZ*(double)lenZ;
		int beginIn = (int)Math.round((double)beginB/(double)upZ*(double)lenZ);
		int temp1 = 0;
		for(int i=0;i<brainNumList.length-1;i++) {
			double lena = altasList[i+1]-altasList[0];
			int pres = (int)Math.round(lenCZ*(lena/lenAZ))-temp1;
			temp1 += pres;
			int ratioTemp = brainNumList[i+1]-brainNumList[i];
			double ratio = (double)pres/(double)ratioTemp;
			for(int t=0;t<ratioTemp;t++) {
				int loc = (int)Math.round(ratio*((double)t+0.5))+temp1-pres;
				if(loc<=0) loc = 1;
				if(loc>lenZ) loc = lenZ;
				if(loc+beginIn<=lenZ)
					annotationOutStack.setProcessor(inStack.getProcessor(loc+beginIn), t+brainNumList[i]);
				else 
					LogIn.error("error");
			}
			
		}
		annotationOut.setStack(annotationOutStack);
		return annotationOut;
		
	}
	
	public static ImagePlus zRestoreShort(String brainNum,String altas,ImagePlus in,int upZ){
		int lenX = in.getWidth(), lenY = in.getHeight(),lenZ = in.getStackSize();
		
		ImagePlus annotationOut = NewImage.createImage("annotationOut", lenX, lenY, upZ,in.getBitDepth(), NewImage.FILL_BLACK);
		
		String[] brainNumStrList = brainNum.split(",");
		String[] altasStrList = altas.split(",");
		int lenNum = altasStrList.length;
		
		int[] brainNumList = new int[lenNum];
		int[] altasList = new int[lenNum];
		
		for(int i=0;i<lenNum;i++){
			brainNumList[i] = Integer.valueOf(brainNumStrList[i]);
			altasList[i] = Integer.valueOf(altasStrList[i]);
			if(altasList[i]!=1)
			altasList[i] = (altasList[i]*5-2)*lenZ/GlobalValue.ALLENLENZ;
		}
		
		ImageStack annotationOutStack = annotationOut.getImageStack(),
				inStack = in.getImageStack();
		
		for(int i=1;i<lenNum;i++){
			ImagePlus tempResult = NewImage.createShortImage("temp", lenX, brainNumList[i]-brainNumList[i-1]+1, lenY, NewImage.FILL_BLACK);
			ImagePlus tempIn = NewImage.createShortImage("tempIn", lenX, altasList[i]-altasList[i-1]+1, lenY, NewImage.FILL_BLACK);

			ImageStack tempResultStack = tempResult.getImageStack();
			ImageStack tempInStack = tempIn.getImageStack();
			
			for(int z=altasList[i-1]-1;z<altasList[i];z++){
				for(int y=0;y<lenY;y++){
					for(int x=0;x<lenX;x++){
						tempInStack.setVoxel(x, z-altasList[i-1]+1, y, inStack.getVoxel(x, y, z));
					}
				}
			}
			for(int z=1;z<=tempIn.getStackSize();z++){
				tempResultStack.setProcessor(tempInStack.getProcessor(z).resize(lenX, brainNumList[i]-brainNumList[i-1]+1), z);
			}
			
			for(int x=0;x<lenX;x++){
				for(int y=brainNumList[i-1]-1;y<brainNumList[i];y++){
					for(int z=0;z<lenY;z++){
						annotationOutStack.setVoxel(x, z, y, tempResultStack.getVoxel(x, y-brainNumList[i-1]+1, z));
					}
				}
			}
		}
		
		annotationOut.setStack(annotationOutStack);
		return annotationOut;
	}
	
	public static ImagePlus zRestoreFloat(String brainNum,String altas,ImagePlus in,int upZ){
		int lenX = in.getWidth(), lenY = in.getHeight();
		
		ImagePlus annotationOut = NewImage.createFloatImage("annotationOut", lenX, lenY, upZ, NewImage.FILL_BLACK);
		
		String[] brainNumStrList = brainNum.split(",");
		String[] altasStrList = altas.split(",");
		int lenNum = altasStrList.length;
		
		int[] brainNumList = new int[lenNum];
		int[] altasList = new int[lenNum];
		
		for(int i=0;i<lenNum;i++){
			brainNumList[i] = Integer.valueOf(brainNumStrList[i]);
			altasList[i] = Integer.valueOf(altasStrList[i]);
			if(altasList[i]!=1)
			altasList[i] = altasList[i]*5-2;
		}
		
		ImageStack annotationOutStack = annotationOut.getImageStack(),
				inStack = in.getImageStack();
		
		for(int i=1;i<lenNum;i++){
			ImagePlus tempResult = NewImage.createFloatImage("temp", lenX, brainNumList[i]-brainNumList[i-1]+1, lenY, NewImage.FILL_BLACK);
			ImagePlus tempIn = NewImage.createFloatImage("tempIn", lenX, altasList[i]-altasList[i-1]+1, lenY, NewImage.FILL_BLACK);

			ImageStack tempResultStack = tempResult.getImageStack();
			ImageStack tempInStack = tempIn.getImageStack();
			
			for(int z=altasList[i-1]-1;z<altasList[i];z++){
				for(int y=0;y<lenY;y++){
					for(int x=0;x<lenX;x++){
						tempInStack.setVoxel(x, z-altasList[i-1]+1, y, inStack.getVoxel(x, y, z));
					}
				}
			}
			for(int z=1;z<=tempIn.getStackSize();z++){
				tempResultStack.setProcessor(tempInStack.getProcessor(z).resize(lenX, brainNumList[i]-brainNumList[i-1]+1), z);
			}
			
			for(int x=0;x<lenX;x++){
				for(int y=brainNumList[i-1]-1;y<brainNumList[i];y++){
					for(int z=0;z<lenY;z++){
						annotationOutStack.setVoxel(x, z, y, tempResultStack.getVoxel(x, y-brainNumList[i-1]+1, z));
					}
				}
			}
		}
		
		annotationOut.setStack(annotationOutStack);
		return annotationOut;
	}
}
