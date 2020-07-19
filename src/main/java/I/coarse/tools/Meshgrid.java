package I.coarse.tools;

import I.entity.MeshGrid3DEntity;
/**
 * 
 * @author zengweilin
 *
 */
public class Meshgrid {

	public MeshGrid3DEntity getMeshGrid3D(double[] x,double[] y, double[] z){
		MeshGrid3DEntity outMeshGrid3DEntity = new MeshGrid3DEntity();
		int lenX = x.length,lenY = y.length, lenZ = z.length;
		double[][][] xx = new double[lenZ][lenY][lenX];
		double[][][] yy = new double[lenZ][lenY][lenX];
		double[][][] zz = new double[lenZ][lenY][lenX];
		
		for(int iz=0;iz<lenZ;iz++){
			for(int iy=0;iy<lenY;iy++){
				for(int ix=0;ix<lenX;ix++){
					xx[iz][iy][ix] = x[ix]/(double)(lenX);
				}
			}
		}
		
		for(int iz=0;iz<lenZ;iz++){
			for(int iy=0;iy<lenY;iy++){
				for(int ix=0;ix<lenX;ix++){
					yy[iz][iy][ix] = y[iy]/(double)(lenY);
				}
			}
		}
		
		for(int iz=0;iz<lenZ;iz++){
			for(int iy=0;iy<lenY;iy++){
				for(int ix=0;ix<lenX;ix++){
					zz[iz][iy][ix] = z[iz]/(double)(lenZ);
				}
			}
		}

		outMeshGrid3DEntity.setXx(xx);
		outMeshGrid3DEntity.setYy(yy);
		outMeshGrid3DEntity.setZz(zz);
		return outMeshGrid3DEntity;
	}
	
}
