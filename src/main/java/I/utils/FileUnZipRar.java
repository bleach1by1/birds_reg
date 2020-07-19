package I.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
 
public class FileUnZipRar {
    /**
          * 解压
     *
     * @param sourceFile
     * @param toFolder
     */
    public static void zipRarToFile(String fileName, String sourceFile, String toFolder) throws Exception {
        int pos = fileName.lastIndexOf(".");
        String extName = fileName.substring(pos + 1).toLowerCase();
        File pushFile = new File(sourceFile);
        File descFile = new File(toFolder);
        if (!descFile.exists()) {
            descFile.mkdirs();
        }
        //解压目的文件
        String descDir = toFolder + "\\" + fileName.substring(0, pos) + "\\";
        //开始解压zip
        if (extName.equals("zip")) {
            FileUnZipRar.unZipFiles(pushFile, descDir);
        } else if (extName.equals("rar")) {
            //开始解压rar
            FileUnZipRar.unRarFile(pushFile.getAbsolutePath(), descDir);
        }
    }
 
    /**
     * 获取所有文件
     *
     * @param filelist
     * @param strPath
     * @return
     */
    public static List<File> getFileList(List<File> filelist, String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                //String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(filelist, files[i].getAbsolutePath()); // 获取文件绝对路径
                } else {
                    filelist.add(files[i]);
                    continue;
                }
            }
 
        }
        return filelist;
    }
 
    /**
          * 解压到指定目录
     *
     * @param zipPath
     * @param descDir
     * @author
     */
    public static void unZipFiles(String zipPath, String descDir) throws IOException {
        unZipFiles(new File(zipPath), descDir);
    }
 
    /**
          * 解压文件到指定目录
     *
     * @param zipFile
     * @param descDir
     * @author isea533
     */
    @SuppressWarnings("rawtypes")
    public static void unZipFiles(File zipFile, String descDir) throws IOException {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = new ZipFile(zipFile);
        for (Enumeration entries = zip.getEntries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if (!file.exists()) {
                file.mkdirs();
            }
            
            if (new File(outPath).isDirectory()) {
                continue;
            }
            //输出文件路径信息
            System.out.println(outPath);
 
            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
        }
    }
 
    /**
          * 根据原始rar路径，解压到指定文件夹下.
     *
     * @param srcRarPath       原始rar路径
     * @param dstDirectoryPath 解压到的文件夹
     */
    public static void unRarFile(String srcRarPath, String dstDirectoryPath) {
        if (!srcRarPath.toLowerCase().endsWith(".rar")) {
            System.out.println("非rar文件！");
            return;
        }
        File dstDiretory = new File(dstDirectoryPath);
        if (!dstDiretory.exists()) {// 目标目录不存在时，创建该文件夹
            dstDiretory.mkdirs();
        }
        Archive a = null;
        try {
            a = new Archive(new File(srcRarPath));
            if (a != null) {
                //a.getMainHeader().print(); // 打印文件信息.
                FileHeader fh = a.nextFileHeader();
                while (fh != null) {
                    if (fh.isDirectory()) { // 文件夹
                        File fol = new File(dstDirectoryPath + File.separator
                                + fh.getFileNameString());
                        fol.mkdirs();
                    } else { // 文件
                        File out = new File(dstDirectoryPath + File.separator
                                + fh.getFileNameString().trim());
                        try {
                            if (!out.exists()) {
                                if (!out.getParentFile().exists()) {
                                    out.getParentFile().mkdirs();
                                }
                                out.createNewFile();
                            }
                            FileOutputStream os = new FileOutputStream(out);
                            a.extractFile(fh, os);
                            os.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    fh = a.nextFileHeader();
                }
                a.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) throws Exception{
    	String nameFileUrl = "E:\\brainmapCompare\\test\\temp_delete\\cache\\cache.rar";
    	String nameStr = "cache.rar";
    	String toFileUrl = "E:\\brainmapCompare\\test\\temp_delete\\cache\\to";
    	FileUnZipRar.zipRarToFile(nameStr, nameFileUrl, toFileUrl);
    }
}
