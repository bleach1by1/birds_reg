package I.phasecongruency;


public class FilterGrid implements Cloneable {

    int width;
    int height;
    double[] xR;
    double[] yR;
    double[][] radius;
    boolean swapQ = false;

    FilterGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid();
    }

    protected void grid() {
        xR = new double[width];
        yR = new double[height];
        radius = new double[height][width];
        int signo;
        int impar;
        double inc;
        double semilla;
        impar = width & 0x1;
        inc = (float) 1.0 / (width - impar);
        signo = impar + (width >> 1);
        xR[0] = 0;
        for (int i = 1; i < signo; i++) {
            xR[i] = xR[i - 1] + inc;
        }
        semilla = -(xR[signo - 1] + inc * (1 - impar));
        for (int i = signo; i < width; i++) {
            xR[i] = semilla;
            semilla += inc;
        }
        impar = height & 0x1;
        inc = 1.0 / (height - impar);
        signo = impar + (height >> 1);
        yR[0] = 0;
        for (int i = 1; i < signo; i++) {
            yR[i] = yR[i - 1] + inc;
        }
        semilla = -(yR[signo - 1] + inc * (1 - impar));
        for (int i = signo; i < height; i++) {
            yR[i] = semilla;
            semilla += inc;
        }

        if (swapQ) {
            double[] tmp = new double[width];
            int mitad = width >> 1;
            for (int i = 0; i < width; i++) {
                tmp[(i + mitad) % width] = xR[i];
            }
            System.arraycopy(tmp, 0, xR, 0, width);
            tmp = new double[height];
            mitad = height >> 1;
            for (int i = 0; i < height; i++) {
                tmp[(i + mitad) % height] = yR[i];
            }
            System.arraycopy(tmp, 0, yR, 0, height);
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                radius[i][j] = Math.sqrt(xR[j] * xR[j] + yR[i] * yR[i]);
            }
        }
    }

    void swapQuadrants() {
        int i, j;
        int h2, w2;
        h2 = height >> 1;
        w2 = width >> 1;
        double[][] dTmp = new double[height][width];
        for (i = 0; i < height; i++) {
            for (j = 0; j < width; j++) {
                dTmp[(i + h2) % height][(j + w2) % width] = radius[i][j];
            }
        }
        for (i = 0; i < height; i++) {
            for (j = 0; j < width; j++) {
                radius[i][j] = dTmp[i][j];
            }
        }
    }

    @Override
    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException ex) {
            System.out.println(" No se puede duplicar");
        }
        return obj;
    }
}
