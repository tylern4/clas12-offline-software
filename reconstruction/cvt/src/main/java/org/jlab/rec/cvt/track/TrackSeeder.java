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
	public List<Seed> findSeed(List<Cluster> SVTclusters, org.jlab.rec.cvt.svt.Geometry svt_geo) {
		
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
				seeds.add(seed);
			}
		}
		//end test
		return seeds;
	}
	
	private Track fitSeed(ArrayList<Cross> SVTCrosses, org.jlab.rec.cvt.svt.Geometry svt_geo, Track cand, int fitIter,
			double[] X, double[] Y, double[] Z, double[] Rho, double[] ErrZ, double[] ErrRho, double[] ErrRt, HelicalTrackFitter fitTrk ) {
		X = new double[SVTCrosses.size()+1];
		Y = new double[SVTCrosses.size()+1];
		Z = new double[SVTCrosses.size()+1];
		Rho = new double[SVTCrosses.size()+1];
		ErrZ = new double[SVTCrosses.size()+1];
		ErrRho = new double[SVTCrosses.size()+1];
		ErrRt = new double[SVTCrosses.size()+1];
		
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
	

}
