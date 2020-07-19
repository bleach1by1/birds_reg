package I.coarse.tools;

import java.io.IOException;
/**
 * 
 * @author zengweilin
 *
 */
public class Pchip {

	public static double[] pchip2D(double[] x,double[] y, double[] u){
		double[] h = new double[x.length-1];
		h = Diff.diff2D(x);
		double[] delta = new double[y.length-1];
		double[] yDiff = new double[y.length-1];
		yDiff = Diff.diff2D(y);
		for(int i=0;i<y.length-1;i++){
			delta[i] = yDiff[i]/h[i];
		}
		double[] d = new double[h.length];
		d = PchipSlopes.pchipslopes2D(h, delta);
		
		int n = x.length;
		double[] c = new double[n-1];
		double[] b = new double[n-1];
		for(int i=0;i<n-1;i++){
			c[i] = (3*delta[i] - 2*d[i] - d[i+1])/h[i];
			b[i] = (d[i] - 2*delta[i] + d[i+1])/(h[i]*h[i]);
		}
		int[] k = new int[u.length];
		for(int i=0;i<u.length;i++){
			k[i] = 0;
		}
		for(int j=1;j<n-1;j++){
			for(int i=0;i<u.length;i++){
				if(u[i]>=x[j]){
					k[i] = j;
				}
			}
		}
		double s;
		double[] v = new double[u.length];
		
		for(int i=0;i<u.length;i++){
			s = u[i]-x[k[i]];
			v[i] = y[k[i]]+s*(d[k[i]]+s*(c[k[i]]+s*b[k[i]]));
		}
		return v;
	}
	
	public static void main(String[] args) throws IOException{
		double[] u = new double[61];
		for(int i=0;i<61;i++){
			u[i] = -3+i*0.1;
		}
		double[] x = {-3,-2,-1,0,1,2,3};
		double[] y = {-4,-2,-1,0,1,2,4};
		//pchip p = new pchip();
		double[] out = Pchip.pchip2D(x, y, u);
		for(Double d:out){
			System.out.println(d);
		}
	}
}
