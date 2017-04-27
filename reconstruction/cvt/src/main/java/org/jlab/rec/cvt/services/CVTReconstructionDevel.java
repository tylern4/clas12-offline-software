package org.jlab.rec.cvt.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.TrackSeeder;
import org.jlab.rec.cvt.track.fit.KFitter;
import org.jlab.rec.cvt.trajectory.TrkSwimmer;

/**
 * Service to return reconstructed BST track candidates- the output is in Evio format
 * 
 * @author ziegler
 *
 */

public class CVTReconstructionDevel extends ReconstructionEngine {


	TrackSeeder trseed ;
	KFitter kf;
	
    public CVTReconstructionDevel() {
    	super("CVTCosmics", "ziegler", "4.0"); 	
    	trseed = new TrackSeeder();
    }

    String FieldsConfig="";
	int Run = -1;
	CVTRecConfig config;
	
    @Override
    public boolean processDataEvent(DataEvent event) {
    	config.setRunConditionsParameters(event, FieldsConfig, Run, false, "");
    	
    	this.FieldsConfig=config.getFieldsConfig();
    	this.Run = config.getRun();
		
	    org.jlab.rec.cvt.svt.Geometry SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
	    
		ADCConvertor adcConv = new ADCConvertor();
		
		RecoBankWriter rbc = new RecoBankWriter();
		
		HitReader hitRead = new HitReader();
		hitRead.fetch_SVTHits(event,adcConv,-1,-1, SVTGeom);
		
		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		List<Hit>  svt_hits = hitRead.get_SVTHits();
		if(svt_hits!=null && svt_hits.size()>0)
			hits.addAll(svt_hits);
		
		List<Hit>  bmt_hits = hitRead.get_BMTHits();
		if(bmt_hits!=null && bmt_hits.size()>0)
			hits.addAll(bmt_hits);
		
		
		//II) process the hits		
		List<FittedHit> SVThits = new ArrayList<FittedHit>();
		List<FittedHit> BMThits = new ArrayList<FittedHit>();
		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return true;
		}
		
		List<Cluster> clusters = new ArrayList<Cluster>();
		List<Cluster> SVTclusters = new ArrayList<Cluster>();
		List<Cluster> BMTclusters = new ArrayList<Cluster>();
		
		//2) find the clusters from these hits
		ClusterFinder clusFinder = new ClusterFinder();
		clusters = clusFinder.findClusters(hits);
		
		if(clusters.size()==0) {
			return true;
		}
		
		// fill the fitted hits list.
		if(clusters.size()!=0) {   			
   			for(int i = 0; i<clusters.size(); i++) {
   				if(clusters.get(i).get_Detector()=="SVT") {
   					SVTclusters.add(clusters.get(i));
   					SVThits.addAll(clusters.get(i));
   				}
   				if(clusters.get(i).get_Detector()=="BMT") {
   					BMTclusters.add(clusters.get(i));
   					BMThits.addAll(clusters.get(i));
   				}
   			}
		}
		
		List<Seed> seeds = trseed.findSeed(SVTclusters, SVTGeom);
		for(Seed seed : seeds) {
			kf = new KFitter(seed, SVTGeom);
			kf.runFitter(SVTGeom);
		}
		return true;
		
    }


	
public boolean init() {
		
		TrkSwimmer.getMagneticFields();
		config = new CVTRecConfig();
		return true;
	}
	public static void main(String[] args) throws FileNotFoundException, EvioException{
		
	String inputFile = "/Users/ziegler/Workdir/Files/GEMC/CVT/prot.hipo";
		
		System.err.println(" \n[PROCESSING FILE] : " + inputFile);
		
		CVTReconstructionDevel en = new CVTReconstructionDevel();
		en.init();
		
		int counter = 0;
		
		 HipoDataSource reader = new HipoDataSource();
         reader.open(inputFile);
		
         HipoDataSync writer = new HipoDataSync();
		//Writer
		 String outputFile="/Users/ziegler/Workdir/Files/GEMC/CVT/protRec.hipo";
		 writer.open(outputFile);
		
		long t1=0;
		while(reader.hasEvent() ){
			
			counter++;
		
			DataEvent event = reader.getNextEvent();
			if(counter>0)
				t1 = System.currentTimeMillis();
			
		
			// Processing    
			en.processDataEvent(event);
			writer.writeEvent(event);
			//System.out.println("  EVENT "+counter);
			if(counter>2) break;
			//event.show();
			//if(counter%100==0)
			//System.out.println("run "+counter+" events");
			
		}
		writer.close();
		double t = System.currentTimeMillis()-t1;
		System.out.println(t1+" TOTAL  PROCESSING TIME = "+(t/(float)counter));
	 }
	
}
