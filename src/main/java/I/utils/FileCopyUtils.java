package I.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class FileCopyUtils {

	@SuppressWarnings("resource")
	public static void copyFileUsingFileChannels(File source, File dest) throws IOException {    
	        FileChannel inputChannel = null;    
	        FileChannel outputChannel = null;    
	    try {
	        inputChannel = new FileInputStream(source).getChannel();
	        outputChannel = new FileOutputStream(dest).getChannel();
	        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
	        
	    } finally {
	        inputChannel.close();
	        outputChannel.close();
	    }
	    
	}
	
	public static int fileCopy(InputStream srcFile, String destFilePath){
		int flag = 0;
		File destFile = new File(destFilePath);
		try {
			BufferedInputStream fis = new BufferedInputStream(srcFile);
			FileOutputStream fos = new FileOutputStream(destFile);
			byte[] buf = new byte[1024];
			int c = 0;
			while ((c = fis.read(buf)) != -1) {
				fos.write(buf, 0, c);
			}
			fis.close();
			fos.close();
			flag = 1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return flag;
	}
	
}
