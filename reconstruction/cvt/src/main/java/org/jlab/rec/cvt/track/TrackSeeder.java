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
	
	public List<Seed> findSeed(List<Cluster> SVTclusters, org.jlab.rec.cvt.svt.Geometry svt_geo) {
		
		List<Seed> seeds = new ArrayList<Seed>();
		
		double angleRange = 10;
		//test
		// sort the clusters...
		int NbLayers = 8;
		
    	Collections.sort(SVTclusters);
    	List<ArrayList<Cluster>> seedClusters = new ArrayList<ArrayList<Cluster>>();
    	
    	List<ArrayList<ArrayList<Cluster>>> candLists = new ArrayList<ArrayList<ArrayList<Cluster>>>();
    	for(int j =0; j<20; j++) { // max nb trks
    		ArrayList<ArrayList<Cluster>> listsByLayer = new ArrayList<ArrayList<Cluster>>();
	        for(int i =0; i<NbLayers; i++) {
	        	listsByLayer.add(new ArrayList<Cluster>());
	        }
	        candLists.add(listsByLayer); 
    	}
    	int index = 0;
    	
		for(int i =1; i<SVTclusters.size(); i++) {
			Cluster clus = SVTclusters.get(i-1);
			double phi = Math.toDegrees(clus.get(0).get_Strip().get_ImplantPoint().toVector3D().phi());
			Cluster clus1 = SVTclusters.get(i);
			double phi1 = Math.toDegrees(clus1.get(0).get_Strip().get_ImplantPoint().toVector3D().phi());
			if(phi<0)
				phi+=360;
			if(phi1<0)
				phi1+=360;
			
	    	if(Math.abs(phi-phi1)<angleRange || Math.abs(phi-phi1)>360-angleRange) {
	    		if(i==1) {
	    			candLists.get(index).get(clus.get_Layer()-1).add(clus);    	
	    	    	clus.printInfo();
	    		}
	    		candLists.get(index).get(clus1.get_Layer()-1).add(clus1);
	    		clus1.printInfo();
	    		} else {
	    			index++;
	    	}
	    	
		}
		

		int NbCands =1;
		for(int i = 0; i<=index; i++) {
				
				int[] L = new int[NbLayers];
				for(int k =0; k<NbLayers; k++) {					
					L[k] = candLists.get(i).get(k).size(); 
					if(L[k]==0)
						L[k]=1;
					NbCands*=L[k];
				}
				
				List<ArrayList<Cluster>> clusLists = new ArrayList<ArrayList<Cluster>>();
				for(int l =0; l<NbCands; l++)
					clusLists.add(new ArrayList<Cluster>());
				
				int listIdx=0;
				for(int k0 =0; k0<L[0]; k0++) 
					for(int k1 =0; k1<L[1]; k1++)
						for(int k2 =0; k2<L[2]; k2++)
							for(int k3 =0; k3<L[3]; k3++) 
								for(int k4 =0; k4<L[4]; k4++)
									for(int k5 =0; k5<L[5]; k5++) {
										
										if(candLists.get(i).get(0).size()!=0 && candLists.get(i).get(0).get(k0)!=null) {
											clusLists.get(listIdx).add(candLists.get(i).get(0).get(k0));
										}
										if(candLists.get(i).get(1).size()!=0 && candLists.get(i).get(1).get(k1)!=null) {
											clusLists.get(listIdx).add(candLists.get(i).get(1).get(k1));
										}
										if(candLists.get(i).get(2).size()!=0 && candLists.get(i).get(2).get(k2)!=null) {
											clusLists.get(listIdx).add(candLists.get(i).get(2).get(k2));
										}
										if(candLists.get(i).get(3).size()!=0 && candLists.get(i).get(3).get(k3)!=null) {
											clusLists.get(listIdx).add(candLists.get(i).get(3).get(k3));
										}
										if(candLists.get(i).get(4).size()!=0 && candLists.get(i).get(4).get(k4)!=null) {
											clusLists.get(listIdx).add(candLists.get(i).get(4).get(k4));
										}
										if(candLists.get(i).get(5).size()!=0 && candLists.get(i).get(5).get(k5)!=null) {
											clusLists.get(listIdx).add(candLists.get(i).get(5).get(k5)); 
										}
					
				}
				seedClusters.addAll(clusLists);
			
		} 
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
				System.out.println(" Strips err "+err);
				loopIdx++;
			}
			 
			CircleFitter circlefit = new CircleFitter();
	   		 boolean circlefitstatusOK = circlefit.fitStatus(Xs, Ys, Ws, Xs.length); 
	   		System.out.println(" circle fit to clusters "+Constants.LIGHTVEL*5/circlefit.getFit().rho());
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
				System.out.println(" cand "+cand.get_Pt()+" "+cand.get_Pz()+" ");
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
