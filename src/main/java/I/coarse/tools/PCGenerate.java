package I.coarse.tools;

import I.phasecongruency.PCMonoRadix3;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.AutoThresholder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class PCGenerate {
	/*
	private static PCGenerate pcGenerate = null;
	private static PCMonoRadix3 pcMonoRadix3 = null;
	private static AutoThresholder autoThresholder = null;
	
	private PCGenerate(){}
	/*
	public static PCGenerate getPCGenerate(){
		if(pcGenerate==null){
			synchronized (PCGenerate.class) {
				if(pcGenerate==null){
					pcGenerate = new PCGenerate();
					pcMonoRadix3 = new PCMonoRadix3();
					autoThresholder = new AutoThresholder();
				}
			}
		}
		return pcGenerate;
	}
	*/
	public static ImageProcessor image2PC(ImageProcessor input){
		
		PCMonoRadix3 pcMonoRadix3 = new PCMonoRadix3();
		AutoThresholder autoThresholder = new AutoThresholder();
		
		int thr = 0;
		input = input.convertToFloatProcessor();
		thr = autoThresholder.getThreshold("Huang", input.getHistogram());
		int lenX = input.getWidth(),lenY = input.getHeight();
		int size=lenX*lenY;
		
		float[] bt = (float[])input.getPixels();
		for(int s=0;s<size;s++){
			if(bt[s]<thr) {
				bt[s]=0;
			}
		}
		if(thr==0) return input;
		double[][] pcDouMat = null;
		pcDouMat = pcMonoRadix3.run(input);
		
		FloatProcessor ipR= new FloatProcessor(lenX,lenY);
		float[] dt=(float[]) ipR.getPixels();
        
        float max=-10;
        float min=255;
        for(int ii=0;ii<size;ii++)			
        {
            dt[ii]=(float) pcDouMat[(int) Math.ceil(ii/lenX)][ii%lenX];
            if(dt[ii]<min)
                min=dt[ii];
            if(dt[ii]>max)
                max=dt[ii];
            
        }
        ipR.setMinAndMax(min, max);
        pcDouMat = null;
		return ipR;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImagePlus input = new ImagePlus("D:\\brain_map_data\\brain\\4.tif");
        ImageStack inputStack = input.getImageStack();
        
        for(int z=1;z<=input.getStackSize();z++){
        	inputStack.setProcessor(PCGenerate.image2PC(inputStack.getProcessor(z)).convertToByte(true), z);
        }
        input.setStack(inputStack);
        new ImageJ();
        input.show();
	}

}
