package I.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import ij.ImagePlus;
import ij.process.ColorProcessor;

public class WaterMarkTools {
    
	public static ColorProcessor addWaterMark(ColorProcessor colorProcessor, int x, int y, String waterMarkContent,Color markContentColor,Font font) {

        Image srcImg = colorProcessor.getBufferedImage();
        int srcImgWidth = srcImg.getWidth(null);//��ȡͼƬ�Ŀ�
        int srcImgHeight = srcImg.getHeight(null);//��ȡͼƬ�ĸ�
        // ��ˮӡ
        BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufImg.createGraphics();
        g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
        g.setColor(markContentColor); //����ͼƬ�ı�������ˮӡ��ɫ
        g.setFont(font);              //��������

        g.drawString(waterMarkContent, x, y);  //����ˮӡ
        g.dispose();  
        ImagePlus outImagePlus = new ImagePlus("out", bufImg);
        ColorProcessor out = outImagePlus.getProcessor().convertToColorProcessor();
        return out;
    }
    public int getWatermarkLength(String waterMarkContent, Graphics2D g) {  
        return g.getFontMetrics(g.getFont()).charsWidth(waterMarkContent.toCharArray(), 0, waterMarkContent.length());  
    }  
}
