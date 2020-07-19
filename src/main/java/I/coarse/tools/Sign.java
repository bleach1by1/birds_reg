package I.coarse.tools;
/**
 * 
 * @author zengweilin
 *
 * @param <T>
 */
public class Sign<T> {

	public int sign1D(double in){
		if(in>0) return 1;
		if(in==0) return 0;
		return -1;
	}
	
	public int[][] sign3D(double[][] in){
		int len1=in.length,len2=in[0].length;
		int[][] out = new int[len1][len2];
		for(int i=0;i<len1;i++){
			for(int j=0;j<len2;j++){
				if(in[i][j]>0){
					out[i][j]=1;
				}else if(in[i][j]<0){
					out[i][j]=-1;
				}else if(in[i][j]==0){
					out[i][j]=0;
				}
			}
		}
		return out;
	}
	
	public int[] sign2D(double[] in){
		int len=in.length;
		int[] out = new int[len];
		for(int i=0;i<len;i++){
			if(in[i]>0){
				out[i]=1;
			}else if(in[i]<0){
				out[i]=0;
			}else if(in[i]==0){
				out[i]=0;
			}
		}
		return out;
	}
	
	public int[][] sign3D(int[][] in){
		int len1=in.length,len2=in[0].length;
		for(int i=0;i<len1;i++){
			for(int j=0;j<len2;j++){
				if(in[i][j]>0){
					in[i][j]=1;
				}else if(in[i][j]<0){
					in[i][j]=-1;
				}
			}
		}
		return in;
	}
	
	public int[] sign2D(int[] in){
		int len=in.length;
		for(int i=0;i<len;i++){
			if(in[i]>0){
				in[i]=1;
			}else if(in[i]<0){
				in[i]=-1;
			}
		}
		return in;
	}
}
