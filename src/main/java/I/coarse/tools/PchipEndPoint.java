package I.coarse.tools;
/**
 * 
 * @author zengweilin
 *
 */
public class PchipEndPoint {

	public double pchipendpoint1D(double h1,double h2,double del1,double del2){
		double d = ((2*h1+h2)*del1 - h1*del2)/(h1+h2);
		Sign<Double> s = new Sign<Double>();
		int dJudge = s.sign1D(d),del1Judge = s.sign1D(del1),del2Judge = s.sign1D(del2);
		if(del1Judge!=dJudge){
			return 0;
		}else if(del1Judge!=del2Judge&&Math.abs(d)>Math.abs(3*del1)){
			return 3*del1;
		}
		return d;
	}
}
