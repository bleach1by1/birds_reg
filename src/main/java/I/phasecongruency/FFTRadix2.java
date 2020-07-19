package I.phasecongruency;

import ij.process.ImageProcessor;


public class FFTRadix2 extends Radix2Processor {

    protected boolean isFrequencyDomain;

    protected int n, m;

    protected double cos[];
    protected double sin[];
    protected boolean tabla = false;
    public double real[][];
    public double imag[][];

    public FFTRadix2(ImageProcessor ip) {
        this(ip, false);
        if (ip instanceof FFTRadix2) {
            this.isFrequencyDomain = ((FFTRadix2) ip).isFrequencyDomain;
            this.real = ((FFTRadix2) ip).real.clone();
            this.imag = ((FFTRadix2) ip).imag.clone();
            this.cos = ((FFTRadix2) ip).cos.clone();
            this.sin = ((FFTRadix2) ip).sin.clone();
            this.tabla = ((FFTRadix2) ip).tabla;
            this.n = ((FFTRadix2) ip).n;
            this.m = ((FFTRadix2) ip).m;
        }
    }

    public FFTRadix2(ImageProcessor ip, boolean isFrequencyDomain) {
        super(ip);
        this.isFrequencyDomain = isFrequencyDomain;
        if (this.esCuadradaP2(width, height)) {
            n = getWidth();
            resetRoi();
            m = 0;
            int i;
            i = n;
            while (i > 1) {
                i = i >> 1;
                m++;
            }
        } else {
        }
    }

    public void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;
        j = 0;
        n2 = n / 2;
        for (i = 1; i < n - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j = 0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a += 1 << (m - i - 1);

                for (k = j; k < n; k = k + n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }


    protected void genTabla() {
        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }
    }

    public void fft2() {
        ImageProcessor ip = this;
        int i, j;
        int offset;
        this.isFrequencyDomain = !this.isFrequencyDomain;
        double x[] = new double[n];
        double y[] = new double[n];
        real = new double[n][n];
        imag = new double[n][n];

        float dt[] = (float[]) ip.getPixels();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                real[i][j] = dt[offset + j];
            }
        }

        if (!tabla) {
            cos = new double[n / 2];
            sin = new double[n / 2];
            genTabla();
            tabla = true;
        }
        for (i = 0; i < n; i++) {
            for (j = 0; j < n; j++) {
                x[j] = real[i][j];
                y[j] = imag[i][j];
            }
            fft(x, y);
            for (j = 0; j < n; j++) {
                real[i][j] = x[j];
                imag[i][j] = y[j];
            }
        }
        for (i = 0; i < n; i++) {
            for (j = 0; j < n; j++) {
                x[j] = real[j][i];
                y[j] = imag[j][i];
            }
            fft(x, y);
            for (j = 0; j < n; j++) {
                real[j][i] = x[j];
                imag[j][i] = y[j];
            }
        }

        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                dt[offset + j] = (float) ((real[i][j] - imag[i][j]) / n);
            }
        }
    }

    void fftComplex() {
        int i, j;
        double x[] = new double[n];
        double y[] = new double[n];
        if (!tabla) {
            cos = new double[n / 2];
            sin = new double[n / 2];
            genTabla();
            tabla = true;
        }
        for (i = 0; i < n; i++) {
            for (j = 0; j < n; j++) {
                x[j] = real[i][j];
                y[j] = imag[i][j];
            }
            fft(x, y);
            for (j = 0; j < n; j++) {
                real[i][j] = x[j];
                imag[i][j] = y[j];
            }
        }
        for (i = 0; i < n; i++) {
            for (j = 0; j < n; j++) {
                x[j] = real[j][i];
                y[j] = imag[j][i];
            }
            fft(x, y);
            for (j = 0; j < n; j++) {
                real[j][i] = x[j];
                imag[j][i] = y[j];
            }
        }

    }

    void ifftComplex() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                imag[i][j] *= -1;
            }
        }
        fftComplex();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                imag[i][j] /= (-width * height);
                real[i][j] /= (width * height);
            }
        }
    }

    public void ifft2() {
        if (this.isFrequencyDomain) {
            this.fft2();
        }
    }

    void update() {
        int offset, i, j;
        float dt[] = (float[]) this.getPixels();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                dt[offset + j] = (float) ((real[i][j] - imag[i][j]) / n);
            }
        }
    }

    void espectroFFT2() {
        int offset, i, j;
        float dt[] = (float[]) this.getPixels();
        this.swapQuadrants();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                dt[offset + j] = (float) (32 * Math.log(Math.sqrt(real[i][j] * real[i][j] + imag[i][j] * imag[i][j]) / n));
            }
        }
        this.swapQuadrants();
    }

    void especNFFT2() {
        int offset, i, j;
        float dt[] = (float[]) this.getPixels();
        this.swapQuadrants();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                dt[offset + j] = (float) (Math.sqrt(real[i][j] * real[i][j] + imag[i][j] * imag[i][j]));
            }
        }
        this.swapQuadrants();
    }

    void swapQuadrants() {
        int i, j;
        double tmp;
        int v1;
        v1 = n / 2;
        for (i = 0; i < n / 2; i++) {
            for (j = 0; j < n / 2; j++) {
                tmp = this.real[i][j];
                real[i][j] = real[i + v1][j + v1];
                real[i + v1][j + v1] = tmp;
                tmp = real[i + v1][j];
                real[i + v1][j] = real[i][j + v1];
                real[i][j + v1] = tmp;

                tmp = imag[i][j];
                imag[i][j] = imag[i + v1][j + v1];
                imag[i + v1][j + v1] = tmp;
                tmp = imag[i + v1][j];
                imag[i + v1][j] = imag[i][j + v1];
                imag[i][j + v1] = tmp;
            }
        }
    }

}
