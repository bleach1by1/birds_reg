package I.phasecongruency;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class PerComp extends Radix2Processor {

    double[][] real;
    double[][] imag;

    public PerComp(ImageProcessor ip) {
        super(ip);
    }

	public void perComp() {
        if (this.esCuadradaP2(width, height)) {
            int n;//width and height of the image
            float[] fhtdata;
            ImageProcessor ipBordes = extraeBordes(this);
            n = ipBordes.getWidth();
            //Domain in frequency space
            double[] dominio = new double[n];
            double inc = 2 * Math.PI / n; //Increase
            double ac = 0;
            for (int i = 0; i < n; i++) {
                dominio[i] = ac;
                ac += inc;
            }

            FFTRadix2 fft = new FFTRadix2(ipBordes);//Result ip
            fft.fft2();
            real = fft.real;
            imag = fft.imag;
            for (int i = 0; i < n; i++) {
                double funH;
                for (int j = 0; j < n; j++) {
                    funH = (2 * (2 - Math.cos(dominio[j]) - Math.cos(dominio[i])));
                    funH = funH == 0 ? 0 : 1 / (funH);
                    real[i][j] *= funH;
                    imag[i][j] *= funH;
                }
            }
            double[][] sr = real.clone();
            double[][] si = imag.clone();
            fft.setPixels(this.getPixels());
            fft.fft2();
            real = fft.real;
            imag = fft.imag;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    real[i][j] -= sr[i][j];
                    imag[i][j] -= si[i][j];
                }
            }
            real = fft.real.clone();
            imag = fft.imag.clone();

            fft.update();
            fft.fft2();
            fhtdata = (float[]) fft.getPixels();
            this.setPixels(fhtdata);
        } else {
        }
    }

    ImageProcessor extraeBordes(ImageProcessor ip) {
        int wd = ip.getWidth();
        int ht = ip.getHeight();
        if (!(ip instanceof FloatProcessor)) {
            throw new RuntimeException("Error, el procesador debe ser float ");
        }

        ImageProcessor ipBordes = ip.createProcessor(wd, ht);

        float[] dtBordes = (float[]) ipBordes.getPixels();
        float[] dtImg = (float[]) ip.getPixels();
        float val;
        // Extract the edges
        int offsetx = wd * (ht - 1);// Start last row
        int offsety = 0;
        //Top-bottom rows.
        for (int i = 0; i < wd; i++) {
            val = (dtImg[i] - dtImg[offsetx]);
            dtBordes[i] = val;
            val = (-dtBordes[i]);
            dtBordes[offsetx] = val;
            offsetx++;
        }

        //Left-right columns
        for (int i = 0; i < ht; i++) {
            val = dtBordes[offsety] + dtImg[offsety] - dtImg[offsety + wd - 1];
            dtBordes[offsety] = val;
            val = dtBordes[offsety + wd - 1] - dtBordes[offsety];
            dtBordes[offsety + wd - 1] = val;
            offsety += wd;
        }

        return ipBordes;
    }
}
