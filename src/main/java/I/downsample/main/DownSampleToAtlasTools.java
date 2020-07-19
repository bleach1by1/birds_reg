package I.downsample.main;

import ij.ImagePlus;
import ij.ImageStack;

import java.awt.Frame;
import java.io.IOException;

import javax.swing.JOptionPane;

public class DownSampleToAtlasTools {

	public static void main(String[] args) throws IOException{
		DownSample d = DownSample.getDownsample();
		Frame f = new Frame();
		f.setSize(500, 780);
		f.add(d.location());
		f.setVisible(true);
	}
	
	public static int[] StringToInt(String[] in){
		int[] out = new int[in.length];
		for(int i=0;i<in.length;i++){
			out[i] = Integer.valueOf(in[i]);
		}
		return out;
	}
	
	public ImagePlus downSample(double[][] in, ImagePlus doneSampleImage){
		ImageStack imageStack = doneSampleImage.getImageStack();
		ImageStack outStack = new ImageStack(doneSampleImage.getDimensions()[0], doneSampleImage.getDimensions()[1]);
		int lenIn = in.length;
		int end, it;
		for(int st=0;st<lenIn;st++){
			end = (int)in[st][1];it = (int)in[st][2];
			for(int i=it-1;i>=0;i--){
				if(end-(int)(in[st][3]*i)>=in[st][0]) outStack.addSlice(imageStack.getProcessor(end-(int)(in[st][3]*i)));
			}
		}
		doneSampleImage.setStack(outStack);
		
		return doneSampleImage;
	}
	
	public ImagePlus backDoneSampleTiff(double[] in,ImagePlus doneSampleImage){
		ImageStack imageStack = doneSampleImage.getImageStack();
		ImageStack outStack = new ImageStack(doneSampleImage.getDimensions()[0], doneSampleImage.getDimensions()[1]);
		int begin = (int)in[0], it = (int)in[2];
		double inside = (in[1]-in[0]+1)/in[2];
		for(int i=1;i<=it;i++){
			if((int)(inside*i)+begin-1<=in[1]) outStack.addSlice(imageStack.getProcessor((int)(inside*i)+begin-1));
		}
		doneSampleImage.setStack(outStack);
		return doneSampleImage;
	}
	
	public ImagePlus topDoneSampleTiff(double[] in,ImagePlus doneSampleImage){
		ImageStack imageStack = doneSampleImage.getImageStack();
		ImageStack outStack = new ImageStack(doneSampleImage.getDimensions()[0], doneSampleImage.getDimensions()[1]);
		int begin = (int)in[0], it = (int)in[2];
		double inside = (in[1]-in[0]+1)/in[2];
		for(int i=0;i<it;i++){
			if((int)(inside*i)+begin-1<=in[1]) outStack.addSlice(imageStack.getProcessor((int)(inside*i)+begin-1));
		}
		doneSampleImage.setStack(outStack);
		return doneSampleImage;
	}
	
	public ImagePlus middleDoneSampleTiff(double[] in,ImagePlus doneSampleImage){
		ImageStack imageStack = doneSampleImage.getImageStack();
		ImageStack outStack = new ImageStack(doneSampleImage.getDimensions()[0], doneSampleImage.getDimensions()[1]);
		int begin = (int)in[0], it = (int)in[2];
		double inside = (in[1]-in[0]+1)/in[2];
		for(int i=0;i<it;i++){
			if((int)(inside*i+inside/2)+begin-1<=in[1]) outStack.addSlice(imageStack.getProcessor((int)(inside*i+inside/2)+begin-1));
		}
		doneSampleImage.setStack(outStack);
		return doneSampleImage;
	}
	
	public ImagePlus downSampleOurBrain(int[] ourBrainFloors,int[] altasFloors,ImagePlus ourBrainImage){
		if(ourBrainFloors.length!=altasFloors.length){
			JOptionPane.showMessageDialog(null, "please input the same length data!");
			return ourBrainImage;
		}
		if(ourBrainFloors.length<=1){
			JOptionPane.showMessageDialog(null, "please input one more data!");
			return ourBrainImage;
		}
		
		ImageStack ourBrainStack = new ImageStack();
		ImageStack outStack = new ImageStack(570, 400);
		ourBrainStack = ourBrainImage.getStack();
		int beginOurBrain = ourBrainFloors[0], beginAltas = altasFloors[0];
		int altasStackNum,ourBrainStackNum;
		
		for(int i = 1;i<=beginAltas;i++){
			outStack.addSlice(ourBrainStack.getProcessor(beginOurBrain/beginAltas*i).resize(570, 400));
		}
		
		for(int i=1;i<ourBrainFloors.length-1;i++){
			altasStackNum = altasFloors[i]-beginAltas+1;
			ourBrainStackNum = ourBrainFloors[i]-beginOurBrain+1;
			for(int j=beginAltas+1;j<=altasFloors[i];j++){
				outStack.addSlice(ourBrainStack.getProcessor(ourBrainStackNum/altasStackNum*j).resize(570, 400));
			}
			beginOurBrain = ourBrainFloors[i];
			beginAltas = altasFloors[i];
		}
		altasStackNum = 660-beginAltas+1;
		ourBrainStackNum = ourBrainImage.getImageStackSize()-beginOurBrain+1;
		for(int i = beginAltas+1;i<=660;i++){
			outStack.addSlice(ourBrainStack.getProcessor(ourBrainStackNum/altasStackNum*i).resize(570, 400));
		}
		ImagePlus outImage = new ImagePlus();
		outImage.setStack(outStack);
		return outImage;
	}
}
