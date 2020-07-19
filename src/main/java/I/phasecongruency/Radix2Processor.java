package I.phasecongruency;

import java.awt.Rectangle;

import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Radix2Processor extends FloatProcessor {

    protected boolean cuadradaP2; // If the image is originally square P2
    protected int originalWidth;
    protected int originalHeight;
    protected double f=1.5;

    public Radix2Processor(ImageProcessor ip) {
        super(ip.getWidth(), ip.getHeight(), (float[]) ((ip instanceof FloatProcessor) ? ip.duplicate().getPixels() : ip.convertToFloat().getPixels()), null);
        if (ip instanceof Radix2Processor) {
            originalWidth = ((Radix2Processor) ip).getOriginalWidth();
            originalHeight = ((Radix2Processor) ip).getOriginalHeight();
            cuadradaP2 = ((Radix2Processor) ip).getCuadradaP2();
            f=((Radix2Processor) ip).getf();
        } else {
            this.originalHeight = this.height;
            this.originalWidth = this.width;
            this.cuadradaP2 = esCuadradaP2(this.width, this.height);
            if(Math.max(this.height, this.width)>512)
                f=1;
        }
    }

    public boolean esCuadradaP2(int wd, int ht) {
        int i = 2;
        while (i < wd) {
            i <<= 1;
        }
        return i == wd && wd == ht;
    }
    public Radix2Processor adjustRadix2() {
        if (esCuadradaP2(this.width, this.height)) {
            return this;
        }
        Rectangle roiRect = this.getRoi();
        int maxN = (int) (f*Math.max(roiRect.width, roiRect.height));

        int i = 2;
        while (i < maxN) {
            i *= 2;
        }
        
        Rectangle fitRect = new Rectangle();
        fitRect.x = (int) Math.round((i - roiRect.width) / 2.0);
        fitRect.y = (int) Math.round((i - roiRect.height) / 2.0);
        fitRect.width = roiRect.width;
        fitRect.height = roiRect.height;

        return tileMirror(i, i, fitRect.x, fitRect.y);
    }

    public Radix2Processor originalImage() {
        if (width != originalWidth && height != originalHeight
                && this.esCuadradaP2(width, height)) {
            ImageProcessor ipR;
            int i = 2;
            int maxN = (int) (f*Math.max(originalWidth, originalHeight));
            while (i < maxN) {
                i *= 2;
            }
            Rectangle fitRect = new Rectangle();
            Rectangle orRect = this.getRoi();
            fitRect.x = (int) Math.round((i - originalWidth) / 2.0);
            fitRect.y = (int) Math.round((i - originalHeight) / 2.0);
            fitRect.width = originalWidth;
            fitRect.height = originalHeight;
            this.setRoi(fitRect);
            ipR = this.crop();
            this.setRoi(orRect);
            Radix2Processor ipNew = new Radix2Processor(ipR);
            return ipNew;
        }
        if (width != originalWidth && height != originalHeight
                && !this.esCuadradaP2(width, height)) {
            IJ.error("Hay un error en la gestion de las dimensiones de la imagen");
        }
        return this;
    }

    private Radix2Processor tileMirror(int width, int height, int x, int y) {
        if (x < 0 || x > (width - 1) || y < 0 || y > (height - 1)) {
            IJ.error("Image to be tiled is out of bounds.");
            return null;
        }

        ImageProcessor ipout = this.createProcessor(width, height);
        ImageProcessor ip2 = this.crop();
        int w2 = ip2.getWidth();
        int h2 = ip2.getHeight();

        int i1 = (int) Math.ceil(x / (double) w2);
        int i2 = (int) Math.ceil((width - x) / (double) w2);
        int j1 = (int) Math.ceil(y / (double) h2);
        int j2 = (int) Math.ceil((height - y) / (double) h2);
        
        if ((i1 % 2) > 0.5) {
            ip2.flipHorizontal();
        }
        if ((j1 % 2) > 0.5) {
            ip2.flipVertical();
        }

        for (int i = -i1; i < i2; i += 2) {
            for (int j = -j1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }

        ip2.flipHorizontal();
        for (int i = -i1 + 1; i < i2; i += 2) {
            for (int j = -j1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }

        ip2.flipVertical();
        for (int i = -i1 + 1; i < i2; i += 2) {
            for (int j = -j1 + 1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }

        ip2.flipHorizontal();
        for (int i = -i1; i < i2; i += 2) {
            for (int j = -j1 + 1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }
        Radix2Processor ipNew = new Radix2Processor(ipout);
        ipNew.originalHeight = h2;
        ipNew.originalWidth = w2;
        ipNew.cuadradaP2 = esCuadradaP2(w2, h2);
        return ipNew;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public boolean getCuadradaP2() {
        return cuadradaP2;
    }
    public double getf(){
        return f;
    }
}
