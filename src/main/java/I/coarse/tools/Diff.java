package I.coarse.tools;
/**
 * 
 * @author zengweilin
 *
 */
public class Diff {

	public static double[] diff2D(double[] x){
		int lenX = x.length;
		double[] xx = new double[lenX-1];
		for(int i=0;i<lenX-1;i++){
			xx[i] = x[i+1]-x[i];
		}
		return xx;
	}
	
	public static double[][] diff3D(double[][] x){
		int lenX1 = x.length,lenX2 = x[0].length;
		double[][] xx = new double[lenX1][lenX2-1];
		
		for(int i=0;i<lenX1;i++){
			for(int j=0;j<lenX2-1;j++){
				xx[i][j] = x[i][j+1]-x[i][j];
			}
		}
		return xx;
	}
}
