package I.phasecongruency;
import ij.IJ;

public class LowPassFilter implements Cloneable {

    int width;
    int height;
    int n;
    double cutoff;
    double[][] filter;

    LowPassFilter(int width, int height, double cutoff, int n) {
        if (cutoff > 0.5 || cutoff < 0) {
            IJ.error("The cut-off frequency should be 0-0.5");
        }
        this.width = width;
        this.height = height;
        this.cutoff = cutoff;
        this.n = n;

    }

    public void generate() {
        FilterGrid grid = new FilterGrid(width, height);
        filter = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                filter[i][j] = 1 / (1 + Math.pow(grid.radius[i][j] / cutoff, 2 * n));
            }
        }
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException ex) {
            System.out.println("Cannot duplicate");
        }
        return obj;
    }
}
