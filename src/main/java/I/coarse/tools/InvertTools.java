package I.coarse.tools;

import java.util.ArrayList;
import java.util.List;

import I.coarse.entity.InvertLineEntity;
import I.entity.MeshGrid3DEntity;
/**
 * 
 * @author zengweilin
 *
 */
public class InvertTools {


    
	public static MeshGrid3DEntity produceMeshGrid(int[] size){
		double[] x=new double[size[0]];
		double[] y=new double[size[1]];
		double[] z=new double[size[2]];
		Meshgrid meshgrid = new Meshgrid();
		for(int xx=0;xx<size[0];xx++){
			x[xx] = xx+1;
		}
		for(int yy=0;yy<size[1];yy++){
			y[yy] = yy+1;
		}
		for(int zz=0;zz<size[2];zz++){
			z[zz] = zz+1;
		}
		
		MeshGrid3DEntity m = meshgrid.getMeshGrid3D(x, y, z);
		return m;
	}
	
	/*
	 * size: 0 is x,1 is y,2 is z
	 */
	public static double[][][] invert(List<InvertLineEntity> lineList,int[] size){
		Regress regress = new Regress();
		MeshGrid3DEntity m = produceMeshGrid(size);
		double[][][] X = m.getXx();
		double[][][] Y = m.getYy();
		double[][][] Z = m.getZz();
		List<InvertLineEntity> lineXZList = new ArrayList<InvertLineEntity>();
		List<InvertLineEntity> lineYZList = new ArrayList<InvertLineEntity>();
		List<InvertLineEntity> lineXYList = new ArrayList<InvertLineEntity>();
		for(InvertLineEntity i:lineList){
			if(i.getType().equals("XZ")){
				lineXZList.add(i);
			}else if(i.getType().equals("YZ")){
				lineYZList.add(i);
			}else if(i.getType().equals("XY")){
				lineXYList.add(i);
			}
		}
		
		double judge = 0;
		double x1,x2,y1,y2;
		double[] zp = new double[size[2]];
		for(int i=1;i<size[2]+1;i++){
			zp[i-1] = ((double)i)/(double)(size[2]);
		}
		double[] A = new double[lineYZList.size()];
		double[] B = new double[lineYZList.size()];
		double[] z1 = new double[lineYZList.size()];
		for(int i=0;i<lineYZList.size();i++){
			x1 = (double)(lineYZList.get(i).getBegin().x)/(double)(size[0]);
			x2 = (double)(lineYZList.get(i).getEnd().x)/(double)(size[0]);
			y1 = (double)(lineYZList.get(i).getBegin().y)/(double)(size[1]);
			y2 = (double)(lineYZList.get(i).getEnd().y)/(double)(size[1]);
			z1[i] = (double)(lineYZList.get(i).getStackNum())/(double)(size[2]);
			A[i]=(y2-y1)/(x2*y1-x1*y2);
	        B[i]=(x1-x2)/(x2*y1-x1*y2);
		}
		double[] Ap = Pchip.pchip2D(z1, A, zp);
		double[] Bp = Pchip.pchip2D(z1, B, zp);
		double[][][] Res = new double[size[2]][size[1]][size[0]];

		for(int i=0;i<size[2];i++){
			for(int j=0;j<size[1];j++){
				for(int k=0;k<size[0];k++){
					judge = Ap[i]*X[i][j][k]+Bp[i]*Y[i][j][k]+1;
					if(judge>0){
						Res[i][j][k] = 1;
					}else {
						Res[i][j][k] = 0;
					}
				}
			}
		}
		
		
		if(lineXYList.size()==2) {
			double[][] trZ = new double[2*lineXYList.size()][1];
			double Res_XY;
			double[][] Zp = new double[2*lineXYList.size()][3];
			
			for(int i=0;i<2*lineXYList.size();i++){
				Zp[i][0] = (double)(lineXYList.get(i/2).getBegin().x)/(double)(size[0]);
				Zp[i][1] = (double)(lineXYList.get(i/2).getBegin().y)/(double)(size[1]);
				Zp[i][2] = 1;
				trZ[i][0] = (double)(lineXYList.get(i/2).getStackNum())/(double)(size[2]);
				i++;
				Zp[i][0] = (double)(lineXYList.get(i/2).getEnd().x)/(double)(size[0]);
				Zp[i][1] = (double)(lineXYList.get(i/2).getEnd().y)/(double)(size[1]);
				Zp[i][2] = 1;
				trZ[i][0] = (double)(lineXYList.get(i/2).getStackNum())/(double)(size[2]);
			}
			double[][] p_XY = regress.regress2D(Zp, trZ);
			for(int i=0;i<size[2];i++){
				for(int j=0;j<size[1];j++){
					for(int k=0;k<size[0];k++){
						judge = p_XY[0][0]*X[i][j][k]+p_XY[0][1]*Y[i][j][k]+p_XY[0][2]-Z[i][j][k];
						if(judge>0){
							Res_XY = 1;
						}else{
							Res_XY = 0;
						}
						Res[i][j][k] = Res[i][j][k]*Res_XY+(1-Res[i][j][k])*(1-Res_XY);
					}
				}
			}
		}
		
		if(lineXZList.size()==2) {
			double[][] trY = new double[2*lineXZList.size()][1];
			double Res_XZ;
			double[][] Yp = new double[2*lineXZList.size()][3];
			
			for(int i=0;i<2*lineXZList.size();i+=2){
				Yp[i][0] = (double)(lineXZList.get(i/2).getBegin().x)/(double)(size[0]);
				Yp[i][1] = (double)(lineXZList.get(i/2).getStackNum())/(double)(size[2]);
				Yp[i][2] = 1;
				trY[i][0] = (double)(lineXZList.get(i/2).getBegin().y)/(double)(size[1]);
				Yp[i+1][0] = (double)(lineXZList.get(i/2).getEnd().x)/(double)(size[0]);
				Yp[i+1][1] = (double)(lineXZList.get(i/2).getStackNum())/(double)(size[2]);
				Yp[i+1][2] = 1;
				trY[i+1][0] = (double)(lineXZList.get(i/2).getEnd().y)/(double)(size[1]);
			}
			double[][] p_XZ = regress.regress2D(Yp, trY);
			for(int i=0;i<size[2];i++){
				for(int j=0;j<size[1];j++){
					for(int k=0;k<size[0];k++){
						judge = p_XZ[0][0]*X[i][j][k]+p_XZ[0][1]*Z[i][j][k]+p_XZ[0][2]-Y[i][j][k];
						if(judge>0){
							Res_XZ = 1;
						}else{
							Res_XZ = 0;
						}
						Res[i][j][k] = Res[i][j][k]*Res_XZ+(1-Res[i][j][k])*(1-Res_XZ);
					}
				}
			}
		}
		
		return Res;
	}
	
}
