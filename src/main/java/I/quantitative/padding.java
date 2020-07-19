package I.quantitative;

import java.util.concurrent.CountDownLatch;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;

public class padding{

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int sizeX = 1140, sizeY = 800, sizeZ = 1320;

		new ImageJ();
		
		String inputUrl = "E:\\figure2\\down10\\YH298_Ch02_Stack[1-1075]_DS10_resize.tif";
		ImagePlus input = new ImagePlus(inputUrl);
		ImagePlus output = NewImage.createByteImage("out", sizeX, sizeY, sizeZ, NewImage.FILL_BLACK);
		
		int lenX = input.getWidth(), lenY = input.getHeight(), lenZ = input.getStackSize();
		
		ImageStack inputStack = input.getImageStack();
		ImageStack outputStack = output.getImageStack();
		CountDownLatch count = new CountDownLatch(lenZ);
		for(int z=0;z<lenZ;z++){
			final int zz = z;
			new Thread(()->{
				for(int x=0;x<lenX;x++){
					for(int y=0;y<lenY;y++){
						outputStack.setVoxel(x, y, zz, inputStack.getVoxel(x, y, zz));
					}
				}
				count.countDown();
			}).start();
		}
		try {
			count.await();
		} catch (Exception e) {}
		
		output.setStack(outputStack);
		output.show();
	}

}
