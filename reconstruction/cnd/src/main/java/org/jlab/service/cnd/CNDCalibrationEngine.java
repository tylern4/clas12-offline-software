package org.jlab.service.cnd;

import java.io.IOException;
import java.util.ArrayList;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.cnd.constants.CalibrationConstantsLoader;
import org.jlab.rec.cnd.banks.HitReader;
import org.jlab.rec.cnd.banks.RecoBankWriter;
import org.jlab.rec.cnd.hit.CndHit;
import org.jlab.rec.cnd.hit.CvtGetHTrack;
import org.jlab.rec.cnd.hit.HalfHit;
import org.jlab.rec.cnd.hit.CndHitFinder;

import java.lang.String;
import java.lang.Double;
import java.lang.Integer;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import org.jlab.clas.physics.LorentzVector;

import org.jlab.rec.cnd.cluster.CNDCluster;
import org.jlab.rec.cnd.cluster.CNDClusterFinder;

/**
 * Service to return reconstructed CND Hits - the output is in Hipo format
 * doing clustering job at the end, provide the cluster infos for PID ("rwangcn8@gmail.com")
 *
 *
 */

public class CNDCalibrationEngine extends ReconstructionEngine {


	public CNDCalibrationEngine() {
		super("CND", "chatagnon & WANG", "1.0");
	
	}

	int Run = -1;
	RecoBankWriter rbc;
	//test
	static int enb =0;
	static int ecnd=0;
	static int hcvt=0;
	static int match=0;
	static int posmatch=0;
	static int ctof=0;
	static int ctoftot=0;

	@Override
	public boolean processDataEvent(DataEvent event) {

		//event.show();
		//LOGGER.debug("in data process ");
            
		// update calibration constants based on run number if changed
		setRunConditionsParameters(event);

                ArrayList<HalfHit> halfhits = new ArrayList<HalfHit>();   
		ArrayList<CndHit> hits = new ArrayList<CndHit>();

		//test
//		if(event.hasBank("CVTRec::Tracks")){
//			hcvt++;
//		}

		halfhits = HitReader.getCndHalfHits(event);		
		//1) exit if halfhit list is empty
		if(halfhits.size()==0 ){
			//			LOGGER.debug("fin de process (0) : ");
			//			event.show();
			return true;
		}

		//2) find the CND hits from these half-hits
		CndHitFinder hitFinder = new CndHitFinder();
		hits = hitFinder.findHits(halfhits,0);

		CvtGetHTrack cvttry = new CvtGetHTrack();
		cvttry.getCvtHTrack(event); // get the list of helix associated with the event

		//int flag=0;
		for (CndHit hit : hits){ // findlength for charged particles
			double length =hitFinder.findLength(hit, cvttry.getHelices(),0);
			if (length!=0){
				hit.set_tLength(length); // the path length is non zero only when there is a match with cvt track
				//if(flag==0){match++;}
				//flag=1;
			}

		}

		//	   			GetVertex getVertex = new GetVertex();
		//	   			Point3D vertex = getVertex.getVertex(event);
		//	   			for (CndHit hit : hits){ // check findlengthneutral
		//	   				hitFinder.findLengthNeutral( vertex, hit);
		//		   			}
		//	   			

		//		if(hits.size()!=0){
		//
		//			DataBank outbank = RecoBankWriter.fillCndHitBanks(event, hits);
		////			LOGGER.debug("event before process : ");
		////			event.show();
		//			event.appendBanks(outbank);
		//			//LOGGER.debug("event after process : ");
		//			//event.show();
		//			ecnd++;
		//			if(event.hasBank("CVTRec::Tracks")){
		//				posmatch++;
		//				//event.getBank("MC::Particle").show();
		//				//outbank.show();
		//			}
		//			
		//		}
		////		LOGGER.debug("fin de process : ");
		////		event.show();
		//		return true;
		//	}
		if(hits.size()!=0){

			//          DataBank outbank = RecoBankWriter.fillCndHitBanks(event, hits);
			//          event.appendBanks(outbank);
			// event.show();
		//	LOGGER.debug("in process event ");
			rbc.appendCNDBanks(event,hits);
			//      ecnd++;
			//      if(event.hasBank("CVTRec::Tracks")){
			//              posmatch++;
			//event.getBank("MC::Particle").show();
			//outbank.show();
			//      }
		//	event.show();

		}



		//// clustering of the CND hits
		CNDClusterFinder cndclusterFinder = new CNDClusterFinder();
		ArrayList<CNDCluster> cndclusters = cndclusterFinder.findClusters(hits);
	        

	        /// Filling the banks of CND clusters
	        int size = cndclusters.size();
	        if(size>0){
	                DataBank bank2 =  event.createBank("CND::clusters", size);
	                if (bank2 == null) {
	                        LOGGER.warn("COULD NOT CREATE A CND::clusters BANK!!!!!!");
	                        return false;
	                }
	                for(int i =0; i< size; i++) {
	                        bank2.setInt("id",i, cndclusters.get(i).get_id() );
	                        bank2.setInt("nhits",i, cndclusters.get(i).get_nhits() );
				bank2.setByte("sector",i,  (byte)(1* cndclusters.get(i).get_sector()) );
				bank2.setByte("layer",i,  (byte)(1*  cndclusters.get(i).get_layer()) );
				bank2.setInt("component",i,  cndclusters.get(i).get_component() );
	                        bank2.setFloat("energy",i,   (float)(1.0* cndclusters.get(i).get_energysum()) );
	                        bank2.setFloat("x",i,   (float)(1.0* cndclusters.get(i).get_x()) );
	                        bank2.setFloat("y",i,   (float)(1.0* cndclusters.get(i).get_y()) );
	                        bank2.setFloat("z",i,   (float)(1.0* cndclusters.get(i).get_z()) );
	                        bank2.setFloat("time",i,   (float)(1.0*  cndclusters.get(i).get_time()) );
				bank2.setInt("status",i,   cndclusters.get(i).get_status());
	                }
	                event.appendBanks(bank2);
	        }


		return true;
		
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		rbc = new RecoBankWriter();
		LOGGER.debug("in init ");
		return true;
	}


	public void setRunConditionsParameters(DataEvent event) {
		if(event.hasBank("RUN::config")==false) {
			LOGGER.warn("RUN CONDITIONS NOT READ!");
		}
		else {
			int newRun = Run;        

			DataBank bank = event.getBank("RUN::config");
			newRun = bank.getInt("run", 0);  
			// Load the constants
			//-------------------
			if(Run!=newRun) {
				CalibrationConstantsLoader.Load(newRun,"default"); 
				Run = newRun;
			}
		}

	}



	public static void main (String arg[]) {
		CNDCalibrationEngine en = new CNDCalibrationEngine();

		en.init();
		//String input = "/Users/ziegler/Workdir/Files/GEMC/ForwardTracks/pi-.r100.evio";
		//String input = "/projet/nucleon/silvia/test.hipo";
		//String input = "/projet/nucleon/silvia/ctof_pion.rec.hipo";
		//String input = "/projet/nucleon/silvia/out_ep.hipo";
		//String input = "/projet/nucleon/silvia/out_out_bis.hipo";
		//String input = "/projet/nucleon/silvia/out_bis.hipo";
		//String input = "/projet/nucleon/silvia/test.rec.hipo";
		//String input = "/projet/nucleon/pierre/test_out3.hipo";
		//String input = "/projet/nucleon/silvia/test.hipo";
		String input = "/projet/nucleon/pierre/RecCND/clas_002227.evio.18.hipo";
		//String input = "/projet/nucleon/pierre/RecCND/test.hipo";
		//String input = "/projet/nucleon/silvia/CLARA/out_clasdispr_small.00849.hipo";
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		String outputFile="/projet/nucleon/pierre/RecCND/test1.hipo";
		HipoDataSync  writer = new HipoDataSync();
		writer.open(outputFile);


		while(reader.hasEvent()) {
			enb++;		
			DataEvent event = (DataEvent) reader.getNextEvent();
			//event.show();
			//LOGGER.debug("event nb "+enb);

			//			LOGGER.debug("event avant process ");
			//			event.show();

			//event.getBank("MC::Particle").show();
			//if(event.hasBank("CVTRec::Tracks")){event.getBank("CVTRec::Tracks").show();};
			en.processDataEvent(event);

			//			LOGGER.debug("event après process ");
			//			event.show();

			//LOGGER.debug("avant write ");
			writer.writeEvent(event);
			//LOGGER.debug("après write ");

//				if(event.hasBank("CND::hits")){
//							//event.show();
//				LOGGER.debug("event nb "+enb);
//				event.getBank("CND::hits").show();	
//			event.getBank("CND::adc").show();	
//			event.getBank("CND::tdc").show();	
//				}



			if(enb==30) break;

		}		
		writer.close();

		//some statitics on cvt/cnd matching
		LOGGER.debug("enb "+enb);
		LOGGER.debug("ecnd "+ecnd);
		LOGGER.debug("hcvt "+hcvt);
		LOGGER.debug("posmatch "+posmatch);
		LOGGER.debug("match "+match);
		LOGGER.debug("%match cnd "+100.*match/posmatch);
		LOGGER.debug("Done");


		HipoDataSource  sortie = new HipoDataSource();
		sortie.open(outputFile);

		LOGGER.debug("Fichier de sortie : ");
		while(sortie.hasEvent()) {

			DataEvent event = (DataEvent) sortie.getNextEvent();
			//event.show();

		}		
	}

}


