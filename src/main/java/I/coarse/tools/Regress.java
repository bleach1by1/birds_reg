package I.coarse.tools;
/**
 * 
 * @author zengweilin
 *
 */
public class Regress {

	 public double[][] getConfactor(double[][] data, int h, int v) {
	        int H = data.length;
	        int V = data[0].length;
	        double[][] newdata = new double[H-1][V-1];
	        for(int i=0; i<newdata.length; i++) {
	            if(i < h-1) {
	                for(int j=0; j<newdata[i].length; j++) {
	                    if(j < v-1) {
	                        newdata[i][j] = data[i][j];
	                    }else {
	                        newdata[i][j] = data[i][j+1];
	                    }
	                }
	            }else {
	                for(int j=0; j<newdata[i].length; j++) {
	                    if(j < v-1) {
	                        newdata[i][j] = data[i+1][j];
	                    }else {
	                        newdata[i][j] = data[i+1][j+1];
	                    }
	                }
	            }
	        }

	        return newdata;
	    }

	 
	  /*
	     * 计算行列式的�?
	     */
	    public double getMartrixResult(double[][] data) {
	        /*
	         * 二维矩阵计算
	         */
	        if(data.length == 2) {
	            return data[0][0]*data[1][1] - data[0][1]*data[1][0];
	        }
	        /*
	         * 二维以上的矩阵计�?
	         */
	        double result = 0;
	        int num = data.length;
	        double[] nums = new double[num];
	        for(int i=0; i<data.length; i++) {
	            if(i%2 == 0) {
	                nums[i] = data[0][i] * getMartrixResult(getConfactor(data, 1, i+1));
	            }else {
	                nums[i] = -data[0][i] * getMartrixResult(getConfactor(data, 1, i+1));
	            }
	        }
	        for(int i=0; i<data.length; i++) {
	            result += nums[i];
	        }

	        return result;
	    }

	    public double[][] getReverseMartrix(double[][] data) {
	        double[][] newdata = new double[data.length][data[0].length];
	        double A = getMartrixResult(data);
	        for(int i=0; i<data.length; i++) {
	            for(int j=0; j<data[0].length; j++) {
	                if((i+j)%2 == 0) {
	                    newdata[i][j] = getMartrixResult(getConfactor(data, i+1, j+1)) / A;
	                }else {
	                    newdata[i][j] = -getMartrixResult(getConfactor(data, i+1, j+1)) / A;
	                }

	            }
	        }
	        newdata = trans(newdata);

	        return newdata;
	    }

	    private double[][] trans(double[][] newdata) {
	        // TODO Auto-generated method stub
	        double[][] newdata2 = new double[newdata[0].length][newdata.length];
	        for(int i=0; i<newdata.length; i++) 
	            for(int j=0; j<newdata[0].length; j++) {
	                newdata2[j][i] = newdata[i][j];
	            }
	        return newdata2;
	    }
	    
	    public double[][] transpose(double[][] in){
	    	int x = in[0].length,y = in.length;
	    	double[][] out = new double[x][y];
	    	
	    	for(int yy=0;yy<y;yy++){
	    		for(int xx=0;xx<x;xx++){
	    			out[xx][yy] = in[yy][xx];
	    		}
	    	}
	    	return out;
	    }
	    
	    public double[][] multiply(double[][] x,double[][] y){
	    	int xx = x[0].length,xy=x.length,yx=y[0].length;
	    	double[][] out = new double[xy][yx];
	    	for(int lx=0;lx<yx;lx++){
	    		for(int ly=0;ly<xy;ly++){
	    			out[ly][lx]=0;
	    			for(int i=0;i<xx;i++){
	    				out[ly][lx]+=x[ly][i]*y[i][lx];
	    			}
	    		}
	    	}
	    	return out;
	    }
	    
	    public double[][] regress2D(double[][] x,double[][] y){
	    	double[][] out = new double[1][x[0].length];
	    	double[][] trX = transpose(x);
	    	
	    	out = multiply(multiply(getReverseMartrix(multiply(trX, x)),trX),y);
	    	
	    	return transpose(out);
	    }
	    
	    public static void main(String[] args) {
	        double[][] data = {
		        {1,2,-1 },  
		        {3,1,0 }, 
		        {-1,-1,-2 },
		    };
	        double[][] y = {
	        		{1},
	        		{2},
	        		{3}
	        };
	        Regress rm = new Regress();
	       // rm.getReverseMartrix(data);
	        double[][] out = rm.regress2D(data, y);
	        for(int i=0;i<1;i++){
	        	for(int j=0;j<3;j++){
	        		System.out.print(out[i][j]+" ");
	        	}
	        	System.out.println();
	        }
	    }
}
