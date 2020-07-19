package I.coarse.tools;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author zengweilin
 *
 */
public class PchipSlopes {

	public static double[] pchipslopes2D(double[] h,double[] delta){
		int n = h.length+1;
		double[] d = new double[h.length+1];
		
		List<Integer> k = new ArrayList<Integer>();

		double judge = 0;
		for(int i=0;i<n-2;i++){
			judge = delta[i]*delta[i+1];
			if(judge>0){
				k.add(i+1);
			}
		}
		
		double w1,w2;
		
		for(int i=0;i<k.size();i++){
			w1 = 2*h[k.get(i)]+h[k.get(i)-1];
			w2 = h[k.get(i)]+2*h[k.get(i)-1];
			d[k.get(i)] = (w1+w2)/(w1/delta[k.get(i)-1]+w2/delta[k.get(i)]);
		}
		PchipEndPoint pchipendpoint = new PchipEndPoint();
		d[0] = pchipendpoint.pchipendpoint1D(h[0], h[1], delta[0], delta[1]);
		d[n-1] = pchipendpoint.pchipendpoint1D(h[n-2], h[n-3], delta[n-2], delta[n-3]);
		return d;
	}
}
