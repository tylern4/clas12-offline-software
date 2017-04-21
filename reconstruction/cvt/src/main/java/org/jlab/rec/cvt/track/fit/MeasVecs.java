package org.jlab.rec.cvt.track.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.rec.cvt.svt.Constants;
import org.jlab.rec.cvt.svt.Geometry;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.fit.StateVecs.StateVec;


public class MeasVecs {

	public List<MeasVec> measurements = new ArrayList<MeasVec>();
	
	public class MeasVec implements Comparable<MeasVec>{
		
		public double centroid;
		public double error;
		public int layer;
		public int sector;
		public int k;
		MeasVec(){
		}

		@Override
		public int compareTo(MeasVec arg) {
			int CompLay = this.layer  < arg.layer  ? -1 : this.layer   == arg.layer   ? 0 : 1;
			return CompLay;
		}
		
	}

	public void setMeasVecs(Seed trkcand) {
		
		measurements = new ArrayList<MeasVec>(trkcand.get_Clusters().size());
		MeasVec meas0 = new MeasVec();
		meas0.centroid =0;
		meas0.error =1;
		meas0.sector = 0;
		meas0.layer = 0;
		measurements.add(meas0);
		for(int i =0; i< trkcand.get_Clusters().size(); i++) {
			MeasVec meas = new MeasVec();
			meas.centroid =trkcand.get_Clusters().get(i).get_Centroid();
			meas.error =0.5;
			meas.sector = trkcand.get_Clusters().get(i).get_Sector();
			meas.layer = trkcand.get_Clusters().get(i).get_Layer();
			measurements.add(meas);
		}
		Collections.sort(measurements);
		for(int i =0; i<measurements.size(); i++)
			measurements.get(i).k = i;
	}
	
	
	public double h(StateVec stateVec, Geometry geo) {
		int sec = this.measurements.get(stateVec.k).sector;
		int lay = this.measurements.get(stateVec.k).layer;
		System.out.println("h : sec "+sec+" lay "+lay+" calcStrip "+geo.calcNearestStrip(stateVec.x, stateVec.y, stateVec.z, lay, sec));
		return geo.calcNearestStrip(stateVec.x, stateVec.y, stateVec.z, lay, sec);
	}
	
	public double[] H(StateVec stateVec, StateVecs sv, Geometry geo) {
		StateVec SVplus = null;// = new StateVec(stateVec.k);
		StateVec SVminus = null;// = new StateVec(stateVec.k);
		
		double delta_d_rho = 0.1;
		SVplus = this.reset(SVplus, stateVec, sv);
		SVminus = this.reset(SVminus, stateVec, sv);
		
		SVplus.d_rho = stateVec.d_rho+delta_d_rho/2.;
		SVminus.d_rho = stateVec.d_rho-delta_d_rho/2.;
		
		SVplus =  sv.getStateVecAtModule(stateVec.k, SVplus, geo);
		SVminus =  sv.getStateVecAtModule(stateVec.k, SVminus, geo);
		
		double delta_m_drho = (h(SVplus, geo) - h(SVminus, geo))/delta_d_rho;
		
		double delta_d_phi0 = 0.1;
		SVplus = this.reset(SVplus, stateVec, sv);
		SVminus = this.reset(SVminus, stateVec, sv);
		
		SVplus.phi0 = stateVec.phi0+delta_d_phi0/2.;
		SVminus.phi0 = stateVec.phi0-delta_d_phi0/2.;
		
		SVplus =  sv.getStateVecAtModule(stateVec.k, SVplus, geo);
		SVminus =  sv.getStateVecAtModule(stateVec.k, SVminus, geo);
		
		double delta_m_dphi0 = (h(SVplus, geo) - h(SVminus, geo))/delta_d_phi0;
		
		double delta_d_kappa = 0.1;
		SVplus = this.reset(SVplus, stateVec, sv);
		SVminus = this.reset(SVminus, stateVec, sv);
		
		SVplus.kappa = stateVec.kappa+delta_d_kappa/2.;
		SVminus.kappa = stateVec.kappa-delta_d_kappa/2.;
		
		SVplus =  sv.getStateVecAtModule(stateVec.k, SVplus, geo);
		SVminus =  sv.getStateVecAtModule(stateVec.k, SVminus, geo);
		
		double delta_m_dkappa = (h(SVplus, geo) - h(SVminus, geo))/delta_d_kappa;
		
		double delta_d_dz = 0.1;
		SVplus = this.reset(SVplus, stateVec, sv);
		SVminus = this.reset(SVminus, stateVec, sv);
		
		SVplus.dz = stateVec.dz+delta_d_dz/2.;
		SVminus.dz = stateVec.dz-delta_d_dz/2.;
		
		SVplus =  sv.getStateVecAtModule(stateVec.k, SVplus, geo);
		SVminus =  sv.getStateVecAtModule(stateVec.k, SVminus, geo);
		
		double delta_m_dz = (h(SVplus, geo) - h(SVminus, geo))/delta_d_dz;
		
		double delta_d_tanL = 0.1;
		SVplus = this.reset(SVplus, stateVec, sv);
		SVminus = this.reset(SVminus, stateVec, sv);
		
		SVplus.tanL = stateVec.tanL+delta_d_tanL/2.;
		SVminus.tanL = stateVec.tanL-delta_d_tanL/2.;
		
		SVplus =  sv.getStateVecAtModule(stateVec.k, SVplus, geo);
		SVminus =  sv.getStateVecAtModule(stateVec.k, SVminus, geo);
		
		double delta_m_dtanL = (h(SVplus, geo) - h(SVminus, geo))/delta_d_tanL;
		
		double[] H=  new double[] 
				{ delta_m_drho, delta_m_dphi0, delta_m_dkappa, delta_m_dz, delta_m_dtanL};
		 for(int i = 0; i<5; i++)
			 System.out.println("num H["+i+"] = "+(float)H[i]);
		 this.H2(stateVec, geo);
		 return H;
		
	}
	private StateVec reset(StateVec SVplus, StateVec stateVec, StateVecs sv) {
		SVplus = sv.new StateVec(stateVec.k);
		SVplus.d_rho = stateVec.d_rho;
		SVplus.alpha = stateVec.alpha;
		SVplus.phi0 = stateVec.phi0;
		SVplus.kappa = stateVec.kappa;
		SVplus.dz = stateVec.dz;
		SVplus.tanL = stateVec.tanL;
		
		
		return SVplus;
	}


	public double[] H2(StateVec stateVec, Geometry geo) {
		double[] H = new double[]{0,0,0,0,0};
		//System.out.println(" Projecting to meas plane ...........");
		if(stateVec.k>0) {
			int sec = this.measurements.get(stateVec.k).sector;
			int lay = this.measurements.get(stateVec.k).layer;
			// global rotation angle
			double Glob_rangl = ((double) (sec-1)/(double) Constants.NSECT[lay-1])*2.*Math.PI + Constants.PHI0[lay-1];
			// angle to rotate to global frame
			double Loc_to_Glob_rangl = Glob_rangl-Constants.LOCZAXISROTATION;
			// the intersection of the track with the module plane
			double x = stateVec.x;
			double y = stateVec.y;
			double z = stateVec.z;
			
			// now get this point in the local frame
			
			double lTx = (Constants.MODULERADIUS[lay-1][sec-1])*Math.cos(Glob_rangl);
			double lTy = (Constants.MODULERADIUS[lay-1][sec-1])*Math.sin(Glob_rangl); 
			double lTz = Constants.Z0[lay-1]; 
			
			//rotate and translate in the local module frame
			double cosRotation = Math.cos(Loc_to_Glob_rangl);
			double sinRotation = Math.sin(Loc_to_Glob_rangl);

			double xt=  (x-lTx)*cosRotation +(y-lTy)*sinRotation  + 0.5*Constants.ACTIVESENWIDTH;
			double zt = z - lTz ;
			
			double alphaAng = (double) Constants.STEREOANGLE/(double) (Constants.NSTRIP-1); 
			
			double P = Constants.PITCH;
		
			
			double del_m_del_x =0;
			double del_m_del_y =0;
			double del_m_del_z =0;
			
			if(lay%2==1) {
				 del_m_del_x = -cosRotation/(alphaAng*zt+P);
				 del_m_del_y = -sinRotation/(alphaAng*zt+P);
				 del_m_del_z = alphaAng*(P+xt-Constants.ACTIVESENWIDTH)/((alphaAng*zt+P)*(alphaAng*zt+P));				 
			 }	
			
			if(lay%2==0) {
				 del_m_del_x = cosRotation/(alphaAng*zt+P);
				 del_m_del_y = sinRotation/(alphaAng*zt+P);
				 del_m_del_z = alphaAng*(P-xt)/((alphaAng*zt+P)*(alphaAng*zt+P));			 
			 }
			 
			//del_m_del_x =(geo.calcNearestStrip(stateVec.x+0.5, stateVec.y, stateVec.z, lay, sec)-geo.calcNearestStrip(stateVec.x-0.5, stateVec.y, stateVec.z, lay, sec));
			//del_m_del_y =(geo.calcNearestStrip(stateVec.x, stateVec.y+0.5, stateVec.z, lay, sec)-geo.calcNearestStrip(stateVec.x, stateVec.y-0.5, stateVec.z, lay, sec));
			//del_m_del_z =(geo.calcNearestStrip(stateVec.x, stateVec.y, stateVec.z+0.5, lay, sec)-geo.calcNearestStrip(stateVec.x, stateVec.y, stateVec.z-0.5, lay, sec));
			
			
			// find H
			//==========================================================================//
			// x = x0 + drho * cos(phi0) + alpha/kappa *(cos(phi0) - cos(phi0 + phi) )  //
			// y = y0 + drho * sin(phi0) + alpha/kappa *(sin(phi0) - sin(phi0 + phi) )  //
			// z = z0 + dz  -  alpha/kappa *tanL*phi                                    //
			//==========================================================================//
			
			// get vars
			double drho = stateVec.d_rho;
			double phi0 = stateVec.phi0;
			double phi = stateVec.phi;
			double alpha = stateVec.alpha;
			double kappa = stateVec.kappa;
			//double dz = stateVec.dz;
			double tanL = stateVec.tanL;
			
			double delx_deldrho = Math.cos(phi0);
			double dely_deldrho = Math.sin(phi0);
			double delz_deldrho = 0;
			double delx_delphi0 = -drho*Math.sin(phi0) - alpha/kappa *(Math.sin(phi0) - Math.sin(phi0 + phi) );
			double dely_delphi0 =  drho*Math.cos(phi0) + alpha/kappa *(Math.cos(phi0) - Math.cos(phi0 + phi) );
			double delz_delphi0 = 0;
			double delx_delkappa = -alpha/(kappa*kappa) *(Math.cos(phi0) - Math.cos(phi0 + phi) );
			double dely_delkappa = -alpha/(kappa*kappa) *(Math.sin(phi0) - Math.sin(phi0 + phi) );
			double delz_delkappa = alpha/(kappa*kappa) *tanL*phi;
			double delx_deldz =0;
			double dely_deldz =0;
			double delz_deldz =1;
			double delx_deltanL =0;
			double dely_deltanL =0;
			double delz_deltanL =-alpha/kappa *phi;
			 H =  new double[] 
						{   del_m_del_x*delx_deldrho + del_m_del_y*dely_deldrho + del_m_del_z*delz_deldrho,
							del_m_del_x*delx_delphi0 + del_m_del_y*dely_delphi0 + del_m_del_z*delz_delphi0,
							del_m_del_x*delx_delkappa + del_m_del_y*dely_delkappa + del_m_del_z*delz_delkappa,						    
							del_m_del_x*delx_deldz + del_m_del_y*dely_deldz + del_m_del_z*delz_deldz,
							del_m_del_x*delx_deltanL + del_m_del_y*dely_deltanL + del_m_del_z*delz_deltanL
						
						};
			 for(int i = 0; i<5; i++)
				 System.out.println("H["+i+"] = "+(float)H[i]);
		}
		
		return H;
	}


	
	
}
