package I.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import I.plugin.LogIn;
/**
 * 
 * @author zwl
 *
 */
public class DownloadTools {
	
	/**
	 * Download file from web url
	 * 
	 * @param urlStr 
	 * @param fileName
	 * @param savePath
	 * @throws IOException
	 */
	public static String downLoadFromUrl(String urlStr, String fileName, String savePath) {
		try {
 
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// Set the overtime to 3 seconds
			conn.setConnectTimeout(3 * 1000);
			// Prevent the shielding program from crawling and returning a 403 error
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
 
			InputStream inputStream = conn.getInputStream();
			byte[] getData = readInputStream(inputStream);
 
			File saveDir = new File(savePath);
			if (!saveDir.exists()) {
				saveDir.mkdir();
			}
			File file = new File(saveDir + File.separator + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(getData);
			if (fos != null) {
				fos.close();
			}
			
			if (inputStream != null) {
				inputStream.close();
			}
			
			return saveDir + File.separator + fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
 
	}
 
	/**
	 * Get byte array from input stream
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0, size = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
			size+=len;
			double section = (double)size/(1024.0*1024.0);
			LogIn.schedule("72.05MB", String.format("%.2f", section)+"MB","download:\t");
		}
		bos.close();
		return bos.toByteArray();
	}
	
	public static void main(String[] args) {
		try {
			FileCopyUtils.copyFileUsingFileChannels(new File("E:\\brainmapCompare\\test\\temp_delete\\cache_test\\cache\\Atlas132label.tif"),
					new File("E:\\brainmapCompare\\test\\temp_delete\\cache_test\\Atlas132label.tif"));
			FileCopyUtils.copyFileUsingFileChannels(new File("E:\\brainmapCompare\\test\\temp_delete\\cache_test\\cache\\annotationOrgImage.tif"),
					new File("E:\\brainmapCompare\\test\\temp_delete\\cache_test\\annotationOrgImage.tif"));
			FileCopyUtils.copyFileUsingFileChannels(new File("E:\\brainmapCompare\\test\\temp_delete\\cache_test\\cache\\tempOrgImage.tif"),
					new File("E:\\brainmapCompare\\test\\temp_delete\\cache_test\\tempOrgImage.tif"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
}
