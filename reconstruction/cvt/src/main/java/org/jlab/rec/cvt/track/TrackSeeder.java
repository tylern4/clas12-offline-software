package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.svt.Constants;

public class TrackSeeder {
	private List<ArrayList<Cluster>> seedClusters = new ArrayList<ArrayList<Cluster>>();
	
	double[] phiShift = new double[]{0, 90}; // move the bin edge to handle bin boundaries
	
	public void FindSeedClusters(List<Cluster> SVTclusters) {
		seedClusters.clear();
		List<ArrayList<Cluster>> phi0 = FindSeedClustersFixedBin(SVTclusters, phiShift[0]); 
		List<ArrayList<Cluster>> phi90 = FindSeedClustersFixedBin(SVTclusters, phiShift[1]); 
		if(phi0.size()>phi90.size())
			seedClusters = phi0;
		if(phi90.size()>phi0.size())
			seedClusters = phi90;
		if(phi90.size()==phi0.size()) {
			for(int i = 0; i< phi0.size(); i++) {
				if(phi0.get(i).size() >= phi90.get(i).size()) {
					seedClusters.add(phi0.get(i));
				} else {
					seedClusters.add(phi90.get(i));
				}
			}
		}
	}
	
	public List<ArrayList<Cluster>> FindSeedClustersFixedBin(List<Cluster> SVTclusters, double phiShift) {
		
		int NbLayers = Constants.NLAYR;		
    	Collections.sort(SVTclusters);
    	List<ArrayList<Cluster>> seedClusters = new ArrayList<ArrayList<Cluster>>();   	
    	int nphiBins = 36;
		Cluster[][] ClsArray = new Cluster[nphiBins][NbLayers];
		
		for(int i = 0; i<SVTclusters.size(); i++) {
			double phi = Math.toDegrees(SVTclusters.get(i).get(0).get_Strip().get_ImplantPoint().toVector3D().phi());
			phi+=phiShift;
			if(phi<0)
				phi+=360;
			
			int binIdx = (int) (phi /nphiBins);
			
			ClsArray[binIdx][SVTclusters.get(i).get_Layer()-1] = SVTclusters.get(i);
			
		}
		
		
		for(int b = 0; b< nphiBins; b++)  {
			
			if(ClsArray[b]!=null)  {
				ArrayList<Cluster> hits = new ArrayList<Cluster>() ;

					for(int la=0; la<6; la++) {

						if(ClsArray[b][la]!=null) 
							hits.add(ClsArray[b][la]);
							
					}
					if(hits.size()>3)  {
						seedClusters.add(hits);
					}
			}
		}
		return seedClusters;
		
	}
	public List<Seed> findSeed(List<Cluster> SVTclusters, org.jlab.rec.cvt.svt.Geometry svt_geo, List<Cross> bmt_crosses) {
		
		List<Seed> seeds = new ArrayList<Seed>();
		
		this.FindSeedClusters(SVTclusters);
		
		for(int s =0; s<seedClusters.size(); s++) {
		//	if(seeds.get(s).size()<4)
			//	continue;
			
			double[] Xs = new double[seedClusters.get(s).size()];
			double[] Ys = new double[seedClusters.get(s).size()];
			double[] Ws = new double[seedClusters.get(s).size()];
			
			int loopIdx = 0;
			for(Cluster c : seedClusters.get(s)) {
				
				Xs[loopIdx] = c.get(0).get_Strip().get_MidPoint().x();
				Ys[loopIdx] = c.get(0).get_Strip().get_MidPoint().y();
				double err = svt_geo.getSingleStripResolution(c.get(0).get_Layer(), c.get(0).get_Strip().get_Strip(), Constants.ACTIVESENLEN/2);
				Ws[loopIdx] =1./(err*err);
				loopIdx++;
			}
			 
			CircleFitter circlefit = new CircleFitter();
	   		 boolean circlefitstatusOK = circlefit.fitStatus(Xs, Ys, Ws, Xs.length); 
	   		
	   		if(! circlefitstatusOK)
	   			continue;
	   		CrossMaker cm = new CrossMaker();
	     	// instantiate array of clusters that are sorted by detector (SVT, BMT [C, Z]) and inner/outer layers
			ArrayList<ArrayList<Cluster>> sortedClusters = new ArrayList<ArrayList<Cluster>>();
			// fill the sorted list
			sortedClusters = cm.sortClusterByDetectorAndIO(seedClusters.get(s));
			// array indexes: array index 0 (1) = svt inner (outer) layer clusters, 2 (3) = bmt inner (outer) layers
			ArrayList<Cluster> svt_innerlayrclus = sortedClusters.get(0);
			ArrayList<Cluster> svt_outerlayrclus = sortedClusters.get(1);
			// arrays of BMT and SVT crosses
			ArrayList<Cross> SVTCrosses = cm.findSVTCrosses(svt_innerlayrclus,svt_outerlayrclus, svt_geo);
			Track cand =fitSeed(SVTCrosses, svt_geo, new Track(null), 5,
					new double[SVTCrosses.size()], new double[SVTCrosses.size()], new double[SVTCrosses.size()], new double[SVTCrosses.size()], 
					new double[SVTCrosses.size()], new double[SVTCrosses.size()], new double[SVTCrosses.size()], new HelicalTrackFitter());
			if(cand!=null) {
				Seed seed = new Seed();
				seed.set_Clusters(seedClusters.get(s));
				seed.set_Crosses(SVTCrosses);
				seed.set_Helix(cand.get_helix());
				
				//match to BMT
				if(bmt_crosses!=null && bmt_crosses.size()!=0) {
					this.findCandUsingMicroMegas(seed, bmt_crosses);
					
					//refit using the BMT
					cand =fitSeed(SVTCrosses, svt_geo, new Track(null), 5,
							new double[SVTCrosses.size()], new double[SVTCrosses.size()], new double[SVTCrosses.size()], new double[SVTCrosses.size()], 
							new double[SVTCrosses.size()], new double[SVTCrosses.size()], new double[SVTCrosses.size()], new HelicalTrackFitter());
					if(cand!=null) {
						seed = new Seed();
						seed.set_Clusters(seedClusters.get(s));
						seed.set_Crosses(cand);
						seed.set_Helix(cand.get_helix());
					}
				}
				
				seeds.add(seed);
				
			}
		}
		//end test
		return seeds;
	}
	
	private Track fitSeed(List<Cross> VTCrosses, org.jlab.rec.cvt.svt.Geometry svt_geo, Track cand, int fitIter,
			double[] X, double[] Y, double[] Z, double[] Rho, double[] ErrZ, double[] ErrRho, double[] ErrRt, HelicalTrackFitter fitTrk ) {
		
		int svtSz = 0;
		int bmtZSz =0;
		int bmtCSz =0;
		
		List<Cross> BMTCrossesC = new ArrayList<Cross>();
		List<Cross> BMTCrossesZ = new ArrayList<Cross>();
		List<Cross> SVTCrosses  = new ArrayList<Cross>();
		
		for(Cross c : VTCrosses) {
			if( !(Double.isNaN(c.get_Point().z()) || Double.isNaN(c.get_Point().x()) ))  {
				SVTCrosses.add(c);
			}
			//System.out.println(" cross in seed "+c.printInfo() );
			if(Double.isNaN(c.get_Point().x()) ) {
				BMTCrossesC.add(c);
			}
			if(Double.isNaN(c.get_Point().z()) ) {
				BMTCrossesZ.add(c);
			}
		}
		svtSz = SVTCrosses.size();
		if(BMTCrossesZ!=null)
			bmtZSz = BMTCrossesZ.size();
		if(BMTCrossesC!=null)
			bmtCSz = BMTCrossesC.size();
		
		X = new double[svtSz+1 +bmtZSz];
		Y = new double[svtSz+1 +bmtZSz];
		Z = new double[svtSz+1 +bmtCSz];
		Rho = new double[svtSz+1 +bmtCSz];
		ErrZ = new double[svtSz+1 +bmtCSz];
		ErrRho = new double[svtSz+1 +bmtCSz];
		ErrRt = new double[svtSz+1 +bmtZSz];
		
		cand = new Track(null);
		cand.addAll(SVTCrosses);
		
		fitTrk = new HelicalTrackFitter();
		for(int i = 0; i < fitIter; i++) {
			X[0] = 0;
			Y[0] = 0;
			Z[0] = 0;
			Rho[0] =0;
			ErrRt[0] = org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT;
			ErrZ[0] = org.jlab.rec.cvt.svt.Constants.ZVTXCONSTRAINT;		
			ErrRho[0] = org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT;										
			
			
			for(int j= 1; j<1+SVTCrosses.size(); j++) {
				
				X[j] = SVTCrosses.get(j-1).get_Point().x();
				Y[j] = SVTCrosses.get(j-1).get_Point().y();
				Z[j] = SVTCrosses.get(j-1).get_Point().z();
				Rho[j] = Math.sqrt(SVTCrosses.get(j-1).get_Point().x()*SVTCrosses.get(j-1).get_Point().x() + 
						SVTCrosses.get(j-1).get_Point().y()*SVTCrosses.get(j-1).get_Point().y());
				ErrRho[j] = Math.sqrt(SVTCrosses.get(j-1).get_PointErr().x()*SVTCrosses.get(j-1).get_PointErr().x() + 
						SVTCrosses.get(j-1).get_PointErr().y()*SVTCrosses.get(j-1).get_PointErr().y());
				ErrRt[j] = Math.sqrt(SVTCrosses.get(j-1).get_PointErr().x()*SVTCrosses.get(j-1).get_PointErr().x() + 
						SVTCrosses.get(j-1).get_PointErr().y()*SVTCrosses.get(j-1).get_PointErr().y());
				ErrZ[j] = SVTCrosses.get(j-1).get_PointErr().z();
				
			}
			
			if(bmtZSz>0)
				for(int j= 1+svtSz; j<svtSz+1 +bmtZSz; j++) {			
					X[j] = BMTCrossesZ.get(j-1-svtSz).get_Point().x();
					Y[j] = BMTCrossesZ.get(j-1-svtSz).get_Point().y();
					ErrRt[j] = Math.sqrt(BMTCrossesZ.get(j-1-svtSz).get_PointErr().x()*BMTCrossesZ.get(j-1-svtSz).get_PointErr().x() + 
							BMTCrossesZ.get(j-1-svtSz).get_PointErr().y()*BMTCrossesZ.get(j-1-svtSz).get_PointErr().y());
					
				
				}
			if(bmtCSz>0)
				for(int j= 1+svtSz; j<svtSz+1 +bmtCSz; j++) {			
					Z[j] = BMTCrossesC.get(j-1-svtSz).get_Point().z();
					Rho[j] =  org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[BMTCrossesC.get(j-1-svtSz).get_Region()-1]+org.jlab.rec.cvt.bmt.Constants.LYRTHICKN;
							
					ErrRho[j] = org.jlab.rec.cvt.bmt.Constants.LYRTHICKN/Math.sqrt(12.);
					ErrZ[j] = BMTCrossesC.get(j-1-svtSz).get_PointErr().z();
							
				}

			
			fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
			if(fitTrk.get_helix()==null) 
				return null;
			
			cand = new Track(fitTrk.get_helix());
			cand.addAll(SVTCrosses);
			//cand.set_HelicalTrack(fitTrk.get_helix());				
			cand.update_Crosses(svt_geo);
		}
		return cand;
	}
	
	/**
	 * 
	 * @param trkCand the track seed
	 * @param bmt_cross BMT cross
	 * @return 
	 */
    public ArrayList<Seed> findCandUsingMicroMegas(Seed trkCand,
			List<Cross> bmt_crosses) {
    	
    	ArrayList<Seed>  trkCands =new ArrayList<Seed>();
    	ArrayList<Cross> BMTCcrosses = new ArrayList<Cross>();
    	ArrayList<Cross> BMTZcrosses = new ArrayList<Cross>();
    	for(Cross bmt_cross : bmt_crosses) { 
    		
    		if(!(Double.isNaN(bmt_cross.get_Point().z())))  // C-detector
    			BMTCcrosses.add(bmt_cross);
    		if(!(Double.isNaN(bmt_cross.get_Point().x())))  // Z-detector
    			BMTZcrosses.add(bmt_cross);
    	}
    	
   
    	List<Seed> AllSeeds = new ArrayList<Seed>();
       	if(BMTCcrosses.size()>0)
	    	for(Cross bmt_Ccross : BMTCcrosses) { // C-detector   		    		
	    		if(this.passCcross(trkCand, bmt_Ccross)) {
	    			Seed BMTTrkSeed = new Seed();
	    			BMTTrkSeed.set_Clusters(trkCand.get_Clusters());
	    			BMTTrkSeed.set_Crosses(trkCand.get_Crosses());
	    			BMTTrkSeed.set_Helix(trkCand.get_Helix());  			
	    			BMTTrkSeed.get_Crosses().add(bmt_Ccross); 
	    			AllSeeds.add(BMTTrkSeed); 
	    		}
	    	}
       	if(AllSeeds.size()==0) { // no C-match
       		Seed BMTTrkSeed = new Seed();
			BMTTrkSeed.set_Clusters(trkCand.get_Clusters());
			BMTTrkSeed.set_Crosses(trkCand.get_Crosses());
			BMTTrkSeed.set_Helix(trkCand.get_Helix());   
			AllSeeds.add(BMTTrkSeed);
       	}
    	if(BMTZcrosses.size()>0 )
    		for(int h = 0; h< AllSeeds.size(); h++) {
		    	for(Cross bmt_Zcross : BMTZcrosses) { // Z-detector   			    		
		    		if(this.passZcross(trkCand, bmt_Zcross)) {
		    			Seed BMTTrkSeed = new Seed();
		    			BMTTrkSeed.set_Clusters(trkCand.get_Clusters());
		    			BMTTrkSeed.set_Crosses(trkCand.get_Crosses());
		    			BMTTrkSeed.set_Helix(trkCand.get_Helix());   			
		    			BMTTrkSeed.get_Crosses().add(bmt_Zcross); 
		    			trkCands.add(BMTTrkSeed);
		    		}
		    	}	    	
    		}
    	
    	
		return trkCands;
    }

	private boolean passCcross(Seed trkCand, Cross bmt_Ccross) {
		
		boolean pass = false;
		
		double dzdrsum = trkCand.get_Helix().get_tandip();
    	
		double z_bmt = bmt_Ccross.get_Point().z();
		double r_bmt = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[bmt_Ccross.get_Region()-1];
		double dzdr_bmt = z_bmt/r_bmt;
		if(Math.abs(1-(dzdrsum/(double)(trkCand.get_Crosses().size()))/((dzdrsum+dzdr_bmt)/(double)(trkCand.get_Crosses().size()+1)))<=Constants.dzdrcut)  // add this to the track
			pass =true;
	
		return pass;
	}
    private boolean passZcross(Seed trkCand, Cross bmt_Zcross) {

    	double ave_seed_rad=trkCand.get_Helix().radius();
    	
		double x_bmt = bmt_Zcross.get_Point().x();
		double y_bmt = bmt_Zcross.get_Point().y();
		boolean pass = true;
		for(int i = 0; i< trkCand.get_Crosses().size()-2; i++) {
			if(trkCand.get_Crosses().get(i).get_Point().x() != x_bmt) {
				double rad_withBmt = calc_radOfCurv(trkCand.get_Crosses().get(i).get_Point().x(), trkCand.get_Crosses().get(i+1).get_Point().x(), x_bmt, 
						                            trkCand.get_Crosses().get(i).get_Point().y(), trkCand.get_Crosses().get(i+1).get_Point().y(), y_bmt);
				if(rad_withBmt==0)
					continue;
				
				if(rad_withBmt<Constants.radcut || Math.abs((rad_withBmt-ave_seed_rad)/ave_seed_rad)>0.3) // more than 30% different
					pass = false;
			}
		}
		return pass;
    }
    /**
	 * 
	 * @param x1 cross1 x-coordinate
	 * @param x2 cross2 x-coordinate
	 * @param x3 cross3 x-coordinate
	 * @param y1 cross1 y-coordinate
	 * @param y2 cross2 y-coordinate
	 * @param y3 cross3 y-coordinate
	 * @return  radius of circle containing 3 crosses  in the (x,y) plane
	 */
	private double calc_radOfCurv(double x1, double x2, double x3, double y1, double y2, double y3){
		double radiusOfCurv = 0;
		
		if ( Math.abs(x2-x1)>1.0e-9 && Math.abs(x3-x2)>1.0e-9) {
	        // Find the intersection of the lines joining the innermost to middle and middle to outermost point
	        double ma   = (y2-y1)/(x2-x1);
	        double mb   = (y3-y2)/(x3-x2);
	       
	        if (Math.abs(mb-ma)>1.0e-9) {
	        double xcen = 0.5*(ma*mb*(y1-y3) + mb*(x1+x2) -ma*(x2+x3))/(mb-ma);
	        double ycen = (-1./mb)*(xcen - 0.5*(x2+x3)) + 0.5*(y2+y3);

	        radiusOfCurv = Math.sqrt((x1-xcen)*(x1-xcen)+(y1-ycen)*(y1-ycen));
	        }
		}
		return radiusOfCurv;
	
	}
	
	

}
