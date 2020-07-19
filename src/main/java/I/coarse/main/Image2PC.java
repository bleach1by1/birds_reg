package I.coarse.main;

import java.util.concurrent.CountDownLatch;

import I.coarse.tools.PCGenerate;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;

public class Image2PC {

	public static void main(String[] args) throws Exception{
		String inputUrl = "";
		ImagePlus input = new ImagePlus(inputUrl);
		int lenX = input.getWidth(), lenY = input.getHeight(), lenZ = input.getStackSize();
		ImagePlus output = NewImage.createByteImage("out", lenX, lenY, lenZ, NewImage.FILL_BLACK);
		ImageStack inputStack = input.getImageStack(),outputStack = output.getImageStack();
		int threadNum = 50,batch = lenZ/threadNum+((lenZ%threadNum>0)?1:0);
		for(int i=0;i<batch;i++) {
			int and = ((lenZ-threadNum*i)>=threadNum)?threadNum:(lenZ-threadNum*i);
			CountDownLatch count = new CountDownLatch(and);
			for(int j=threadNum*i;j<threadNum*i+and;j++) {
				final int z = j+1;
				new Thread(()->{
					outputStack.setProcessor(PCGenerate.image2PC(inputStack.getProcessor(z)).convertToByteProcessor(), z);
					System.out.println(z);
					count.countDown();
				}).start();
			}
			count.await();
		}
		
		output.setStack(outputStack);
		output.show();
	}

}
