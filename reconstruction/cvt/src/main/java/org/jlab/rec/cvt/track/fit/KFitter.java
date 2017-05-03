package org.jlab.rec.cvt.track.fit;
import java.util.ArrayList;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.svt.Geometry;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.fit.StateVecs.B;
import org.jlab.rec.cvt.track.fit.StateVecs.StateVec;
import org.jlab.rec.cvt.trajectory.Helix;

import Jama.Matrix;

public class KFitter {

	public boolean setFitFailed = false;
	
	StateVecs sv = new StateVecs();
	MeasVecs mv = new MeasVecs();
	
	public StateVec finalStateVec;
	
	public KFitter(Seed trk, Geometry geo, DataEvent event) {
		this.init(trk, geo, event);
	}

	public void init(Seed trk, Geometry geo, DataEvent event) {
		//Helix helix = trk.get_Helix();
		mv.setMeasVecs(trk, geo);
		if(sv.Layer!=null) {
			sv.Layer.clear();
		} else {
			sv.Layer = new ArrayList<Integer>();
		}
		if(sv.Sector!=null) {
			sv.Sector.clear();
		} else {
			sv.Sector = new ArrayList<Integer>();
		}
		if(sv.X0!=null) {
			sv.X0.clear();
		} else {
			sv.X0 = new ArrayList<Double>();
		}
		if(sv.Y0!=null) {
			sv.Y0.clear();
		} else {
			sv.Y0 = new ArrayList<Double>();
		}
		if(sv.Z0!=null) {
			sv.Z0.clear();
		} else {
			sv.Z0 = new ArrayList<Double>();
		}
		//take first plane along beam line with n = y-dir;
		sv.Layer.add(0);
		sv.Sector.add(0);
		sv.X0.add((double) 0.0);
		sv.Y0.add((double) 0.0);
		sv.Z0.add((double) 0.0);
		for(int i =1; i< mv.measurements.size(); i++) {
			sv.Layer.add(mv.measurements.get(i).layer);
			sv.Sector.add(mv.measurements.get(i).sector);
			//Point3D ref = geo.intersectionOfHelixWithPlane(mv.measurements.get(i).layer, mv.measurements.get(i).sector,  helix) ;
			//ref = new Point3D(0,Constants.MODULERADIUS[mv.measurements.get(i).layer-1][0], 0);
			Point3D ref = new Point3D(0,0,0);
			sv.X0.add(ref.x());
			sv.Y0.add(ref.y());
			sv.Z0.add(ref.z());
		}
		sv.init(trk, this, event);		
	}
	
	public int totNumIter = 5;
	double newChisq = Double.POSITIVE_INFINITY;
	
	public void runFitter(Geometry geo) {
		this.chi2 = 0;	
		this.NDF = sv.X0.size();
		for(int it = 0; it< totNumIter; it++) {
			for(int k=0; k<sv.X0.size()-1; k++) {
				System.out.println(" transporting state ");
				sv.transport(k, k+1, sv.trackTraj.get(k), sv.trackCov.get(k), geo);
					System.out.println((k)+"] trans "+sv.trackTraj.get(k).x+","+sv.trackTraj.get(k).y+","+
							sv.trackTraj.get(k).z+" p "+1./sv.trackTraj.get(k).kappa); 
					System.out.println("To "+(k+1)+"] trans "+sv.trackTraj.get(k+1).x+","+sv.trackTraj.get(k+1).y+","+
							sv.trackTraj.get(k+1).z+" p "+1./sv.trackTraj.get(k).kappa); 
					System.out.println(" Filtering state ...........................................");
					this.filter(k+1, geo);
					System.out.println((k+1)+"] filt "+sv.trackTraj.get(k+1).x+","+sv.trackTraj.get(k+1).y+","+
							sv.trackTraj.get(k+1).z); 
					System.out.println(" Energy loss \n pion "+ (float) sv.trackTraj.get(k+1).get_ELoss()[0]+"\n kaon "+ (float) sv.trackTraj.get(k+1).get_ELoss()[1]+"\n proton "+ (float) sv.trackTraj.get(k+1).get_ELoss()[2]);
				}
			if(it<totNumIter-1)
				this.Rinit();
			}
		}
		
	public void Rinit() {
		Helix helix = sv.setTrackPars(sv.X0.size()-1);

		sv.trackTraj.get(0).x = -helix.get_dca()*Math.sin(helix.get_phi_at_dca());
		sv.trackTraj.get(0).y = helix.get_dca()*Math.cos(helix.get_phi_at_dca());
		sv.trackTraj.get(0).z = helix.get_Z0();
		double xcen    = (1./helix.get_curvature() - helix.get_dca()) * Math.sin(helix.get_phi_at_dca());  
        double ycen    = (-1./helix.get_curvature() + helix.get_dca()) * Math.cos(helix.get_phi_at_dca()); 
        B Bf = sv.new B(0,0,0,0);
        sv.trackTraj.get(0).alpha = Bf.alpha;
        sv.trackTraj.get(0).kappa = Bf.alpha*helix.get_curvature();  
        sv.trackTraj.get(0).phi0 = Math.atan2(ycen, xcen);
		if(sv.trackTraj.get(0).kappa<0)
			sv.trackTraj.get(0).phi0 = Math.atan2(-ycen, -xcen);
		sv.trackTraj.get(0).dz = helix.get_Z0();
		sv.trackTraj.get(0).tanL = helix.get_tandip();
		sv.trackTraj.get(0).d_rho = helix.get_dca();
		sv.trackTraj.get(0).phi = 0;
		
		sv.trackCov.get(0).covMat = sv.trackCov.get(sv.X0.size()-1).covMat;
	}
	public Track OutputTrack(Seed trk, Geometry geo) {
		
		Helix helix = sv.setTrackPars(sv.X0.size()-1);
		Track cand = new Track(helix);
		cand.addAll(trk.get_Crosses());
		
		for(Cross c : cand)
			System.out.println(" before update "+c.printInfo());
		
		for(Cross c : trk.get_Crosses())
			System.out.println(" after update "+c.printInfo());
		cand.finalUpdate_Crosses(geo);
		
		return cand;
	}
	
	public double chi2 = 0;
	public int NDF = 0;
	
	private void filter(int k, Geometry geo) {
		
		if(sv.trackTraj.get(k)!=null && sv.trackCov.get(k).covMat!=null ) {		
			double[] K = new double[5];
			double V   =  mv.measurements.get(k).error; 
			double[] H =  mv.H(sv.trackTraj.get(k), sv, geo);
			
			double[][] HTGH =  new double[][] {
					{H[0]*H[0]/V,H[0]*H[1]/V,H[0]*H[2]/V,H[0]*H[3]/V,H[0]*H[4]/V},
					{H[1]*H[0]/V,H[1]*H[1]/V,H[1]*H[2]/V,H[1]*H[3]/V,H[1]*H[4]/V},
					{H[2]*H[0]/V,H[2]*H[1]/V,H[2]*H[2]/V,H[2]*H[3]/V,H[2]*H[4]/V},
					{H[3]*H[0]/V,H[3]*H[1]/V,H[3]*H[2]/V,H[3]*H[3]/V,H[3]*H[4]/V},
					{H[4]*H[0]/V,H[4]*H[1]/V,H[4]*H[2]/V,H[4]*H[3]/V,H[4]*H[4]/V}
			}; 
			
			Matrix Ci = null;
			this.printMatrix(new Matrix(HTGH));System.err.println("-------------------------------\n");
			if(this.isNonsingular(sv.trackCov.get(k).covMat)==false) {
				System.err.println("Covariance Matrix is non-invertible - quit filter!");
				this.printMatrix(sv.trackCov.get(k).covMat);
				return;
			}
			try {
				Ci = sv.trackCov.get(k).covMat.inverse();
			} catch (Exception e) {				
				return;
			}
			
			Matrix Ca = null;
			try {
				Ca = Ci.plus(new Matrix(HTGH));
			} catch (Exception e) {
				return;
			}
			if(Ca!=null && this.isNonsingular(Ca)==false) {
				System.err.println("Covariance Matrix is non-invertible - quit filter!");
				return;
			}
			if(Ca!=null) {
				if(Ca.inverse()!=null) {
					Matrix CaInv = Ca.inverse();
					sv.trackCov.get(k).covMat = CaInv;
				//System.err.println("Error: e");
				} else {
				return;
				}
			} else {
				return;
			}
			
			for(int j = 0; j < 5; j++) { 
				// the gain matrix
				K[j] = 0;
				for(int i = 0; i < 5; i++)
					K[j]+=H[i]*sv.trackCov.get(k).covMat.get(j, i)/V ;	
				
			}
			
			double h =  mv.h(sv.trackTraj.get(k), geo);
			//double c2 = ((1 - (H[0]*K[0] + H[1]*K[1]))*(1 - (H[0]*K[0] + H[1]*K[1]))*(mv.measurements.get(k).x - h)*(mv.measurements.get(k).x - h)/V);
			
			double drho_filt 	= sv.trackTraj.get(k).d_rho + 	K[0]*(mv.measurements.get(k).centroid - h);
			double phi0_filt 	= sv.trackTraj.get(k).phi0 	+ 	K[1]*(mv.measurements.get(k).centroid - h);
			double kappa_filt	= sv.trackTraj.get(k).kappa + 	K[2]*(mv.measurements.get(k).centroid - h);
			double dz_filt		= sv.trackTraj.get(k).dz 	+ 	K[3]*(mv.measurements.get(k).centroid - h);
			double tanL_filt 	= sv.trackTraj.get(k).tanL 	+ 	K[4]*(mv.measurements.get(k).centroid - h);
			
			
			StateVec fVec = sv.new StateVec( sv.trackTraj.get(k).k);
			fVec.d_rho 	= drho_filt;
			fVec.phi0 	= phi0_filt;
			fVec.kappa 	= kappa_filt;
			fVec.dz 	= dz_filt;
			fVec.tanL 	= tanL_filt; 
			fVec.alpha  = sv.trackTraj.get(k).alpha;
			
			sv.getStateVecAtModule(k, fVec, geo);
			double f_h = mv.h(fVec, geo);
			System.out.println(" measurement = "+mv.measurements.get(k).centroid+" state "+h +" filtered "+f_h);
		
			if((mv.measurements.get(k).centroid - f_h)*(mv.measurements.get(k).centroid - f_h)/V < (mv.measurements.get(k).centroid - h)*(mv.measurements.get(k).centroid - h)/V) {
				
				sv.trackTraj.get(k).d_rho 	= drho_filt;
				sv.trackTraj.get(k).phi0 	= phi0_filt;
				sv.trackTraj.get(k).kappa 	= kappa_filt;
				sv.trackTraj.get(k).dz 		= dz_filt;
				sv.trackTraj.get(k).tanL 	= tanL_filt; 
				sv.getStateVecAtModule(k, sv.trackTraj.get(k), geo);
				
				
			//	sv.trackTraj.put(k, fVec);
			} 
			chi2+=(mv.measurements.get(k).centroid - f_h)*(mv.measurements.get(k).centroid - f_h)/V;
			
		}
	}

	/**
	 * prints the matrix -- used for debugging
	 * @param C matrix
	 */
	public void printMatrix(Matrix C) {
		for(int k = 0; k< 5; k++) {
			System.out.println(C.get(k, 0)+"	"+C.get(k, 1)+"	"+C.get(k, 2)+"	"+C.get(k, 3)+"	"+C.get(k, 4));
		}
	}
	
	private boolean isNonsingular(Matrix mat) {
		
	      for (int j = 0; j < mat.getColumnDimension(); j++) {
	         if (Math.abs(mat.get(j, j)) < 0.00000000001) {
	            return false;
	         }
	      }
	      return true;
	}
	
}
