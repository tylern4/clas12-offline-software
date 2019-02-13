/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.decode.DetectorDataDgtz.ADCData;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class DetectorEventDecoder {
    public static Logger LOGGER = LogManager.getLogger(DetectorEventDecoder.class.getName());
    ConstantsManager  translationManager = new ConstantsManager();
    ConstantsManager  fitterManager      = new ConstantsManager();
    
    List<String>  tablesTrans            = null;
    List<String>  keysTrans              = null;
    
    List<String>  tablesFitter            = null;
    List<String>  keysFitter              = null;
    
    private  int  runNumber               = 10;
    
    private  BasicFADCFitter      basicFitter     = new BasicFADCFitter();
    private  ExtendedFADCFitter   extendedFitter  = new ExtendedFADCFitter();
    private  MVTFitter            mvtFitter       = new MVTFitter();
    
    private  Boolean          useExtendedFitter   = false;
    
    
    
    public DetectorEventDecoder(boolean development){
        if(development==true){
            this.initDecoderDev();
        } else {
            this.initDecoder();
        }
    }
    
    public void setRunNumber(int run){
        this.runNumber = run;
    }
    
    public DetectorEventDecoder(){
        this.initDecoder();
        /*
        keysTrans = Arrays.asList(new String[]{
            "FTCAL","FTHODO","LTCC","EC","FTOF","HTCC","DC"
        });
        
        tablesTrans = Arrays.asList(new String[]{
            "/daq/tt/ftcal","/daq/tt/fthodo","/daq/tt/ltcc",
            "/daq/tt/ec","/daq/tt/ftof","/daq/tt/htcc","/daq/tt/dc"
        });
        
        translationManager.init(keysTrans,tablesTrans);
        
        keysFitter   = Arrays.asList(new String[]{"FTCAL","FTOF","LTCC","EC","HTCC"});
        tablesFitter = Arrays.asList(new String[]{
            "/daq/fadc/ftcal","/daq/fadc/ftof","/daq/fadc/ltcc","/daq/fadc/ec",
            "/daq/fadc/htcc"
        });
        fitterManager.init(keysFitter, tablesFitter);
        */
    }
    
    public final void initDecoderDev(){
        keysTrans = Arrays.asList(new String[]{ "HTCC","BST"} );
        tablesTrans = Arrays.asList(new String[]{ "/daq/tt/clasdev/htcc","/daq/tt/clasdev/svt" });
        
        keysFitter   = Arrays.asList(new String[]{"HTCC"});
        tablesFitter = Arrays.asList(new String[]{"/daq/fadc/clasdev/htcc"});
        translationManager.init(keysTrans,tablesTrans);
        fitterManager.init(keysFitter, tablesFitter);
    }
    
    public final void initDecoder(){
        keysTrans = Arrays.asList(new String[]{
		"FTCAL","FTHODO","FTTRK","LTCC","ECAL","FTOF","HTCC","DC","CTOF","CND","BST","RF","BMT","FMT","RICH","HEL","BAND"
        });
        
        tablesTrans = Arrays.asList(new String[]{
            "/daq/tt/ftcal","/daq/tt/fthodo","/daq/tt/fttrk","/daq/tt/ltcc",
            "/daq/tt/ec","/daq/tt/ftof","/daq/tt/htcc","/daq/tt/dc","/daq/tt/ctof","/daq/tt/cnd","/daq/tt/svt",
            "/daq/tt/rf","/daq/tt/bmt","/daq/tt/fmt","/daq/tt/clasdev/richcosmic","/daq/tt/hel","/daq/tt/band"
        });
        
        translationManager.init(keysTrans,tablesTrans);
        
        keysFitter   = Arrays.asList(new String[]{"FTCAL","FTHODO","FTTRK","FTOF","LTCC","ECAL","HTCC","CTOF","CND","BMT","FMT","HEL","RF","BAND"});
        tablesFitter = Arrays.asList(new String[]{
            "/daq/fadc/ftcal","/daq/fadc/fthodo","/daq/config/fttrk","/daq/fadc/ftof","/daq/fadc/ltcc","/daq/fadc/ec",
            "/daq/fadc/htcc","/daq/fadc/ctof","/daq/fadc/cnd","/daq/config/bmt","/daq/config/fmt","/daq/fadc/hel","/daq/fadc/rf","/daq/fadc/band"
        });
        fitterManager.init(keysFitter, tablesFitter);
    }
    /**
     * Set the flag to use extended fitter instead of basic fitter
     * which simply integrates over given bins inside of the given
     * windows for the pulse. The pulse parameters are provided by 
     * fitterManager (loaded from database).
     * @param flag 
     */
    public void setUseExtendedFitter(boolean flag){
        this.useExtendedFitter = flag;
    }
    /**
     * applies translation table to the digitized data to translate
     * crate,slot channel to sector layer component.
     * @param detectorData 
     */
    public void translate(List<DetectorDataDgtz>  detectorData){
        
        for(DetectorDataDgtz data : detectorData){
            
            int crate    = data.getDescriptor().getCrate();
            int slot     = data.getDescriptor().getSlot();
            int channel  = data.getDescriptor().getChannel();
            //if(crate==69){
	    //LOGGER.debug(" MVT " + crate + " " + slot + 
	    //  "  " + channel);
	// }
            boolean hasBeenAssigned = false;
            
            for(String table : keysTrans){
                IndexedTable  tt = translationManager.getConstants(runNumber, table);
                DetectorType  type = DetectorType.getType(table);

                if(tt.hasEntry(crate,slot,channel)==true){
                    int sector    = tt.getIntValue("sector", crate,slot,channel);
                    int layer     = tt.getIntValue("layer", crate,slot,channel);
                    int component = tt.getIntValue("component", crate,slot,channel);
                    int order     = tt.getIntValue("order", crate,slot,channel);
                    
                    /*if(crate>60&&crate<64){
                        LOGGER.debug(" SVT " + sector + " " + layer + 
                                "  " + component);
                    }*/
                    data.getDescriptor().setSectorLayerComponent(sector, layer, component);
                    data.getDescriptor().setOrder(order);
                    data.getDescriptor().setType(type);
                    for(int i = 0; i < data.getADCSize(); i++) {
                        data.getADCData(i).setOrder(order);
                    }
                    for(int i = 0; i < data.getTDCSize(); i++) {
                        data.getTDCData(i).setOrder(order);
                    }
                }
            }
        }
        //Collections.sort(detectorData);
    }
    
        public void fitPulses(List<DetectorDataDgtz>  detectorData){
        for(DetectorDataDgtz data : detectorData){            
            int crate    = data.getDescriptor().getCrate();
            int slot     = data.getDescriptor().getSlot();
            int channel  = data.getDescriptor().getChannel();
            //LOGGER.debug(" looking for " + crate + "  " 
            //       + slot + " " + channel);
            for(String table : keysFitter){
                //custom MM fitter
            	if( ( (table.equals("BMT"))&&(data.getDescriptor().getType().getName().equals("BMT")) ) 
                 || ( (table.equals("FMT"))&&(data.getDescriptor().getType().getName().equals("FMT")) ) 
                 || ( (table.equals("FTTRK"))&&(data.getDescriptor().getType().getName().equals("FTTRK")) ) ){
                    IndexedTable daq = fitterManager.getConstants(runNumber, table);
                    short adcOffset = (short) daq.getDoubleValue("adc_offset", 0, 0, 0);
                    double fineTimeStampResolution = (byte) daq.getDoubleValue("dream_clock", 0, 0, 0);
                    double samplingTime = (byte) daq.getDoubleValue("sampling_time", 0, 0, 0);
                    int sparseSample = daq.getIntValue("sparse", 0, 0 ,0);
                    if (data.getADCSize() > 0) {
                        ADCData adc = data.getADCData(0);
                        mvtFitter.fit(adcOffset, fineTimeStampResolution, samplingTime, adc.getPulseArray(), adc.getTimeStamp(), sparseSample);
                        adc.setHeight((short) (mvtFitter.adcMax));
                        adc.setTime((int) (mvtFitter.timeMax));
                        adc.setIntegral((int) (mvtFitter.integral));
                        adc.setTimeStamp(mvtFitter.timestamp);
                    }
                } else {
                    IndexedTable  daq = fitterManager.getConstants(runNumber, table);
                    DetectorType  type = DetectorType.getType(table);
                    if(daq.hasEntry(crate,slot,channel)==true){                    
                        //basicFitter.setPulse(0, 4).setPedestal(35, 70);
                        //for(int i = 0; i < data.getADCSize(); i++){
                        //    basicFitter.fit(data.getADCData(i));
                        //}
                        int nsa = daq.getIntValue("nsa", crate,slot,channel);
                        int nsb = daq.getIntValue("nsb", crate,slot,channel);
                        int tet = daq.getIntValue("tet", crate,slot,channel);
                        int ped = 0;
                        if(table.equals("RF")&&data.getDescriptor().getType().getName().equals("RF")) ped = daq.getIntValue("pedestal", crate,slot,channel);
                        if(data.getADCSize()>0){
                            for(int i = 0; i < data.getADCSize(); i++){
                                ADCData adc = data.getADCData(i);
                                if(adc.getPulseSize()>0){
                                    //LOGGER.debug("-----");
                                    //LOGGER.debug(" FITTING PULSE " + 
                                    //        crate + " / " + slot + " / " + channel);
                                    try {
                                        extendedFitter.fit(nsa, nsb, tet, ped, adc.getPulseArray());
                                    } catch (Exception e) {
                                        LOGGER.debug(">>>> error : fitting pulse "
                                                            +  crate + " / " + slot + " / " + channel);
                                    }
                                    //LOGGER.debug(" FIT RESULT = " + extendedFitter.adc + " / "
                                    //        + this.extendedFitter.t0 + " / " + this.extendedFitter.ped);
                                    int adc_corrected = extendedFitter.adc + extendedFitter.ped*(nsa+nsb);
                                    adc.setHeight((short) this.extendedFitter.pulsePeakValue);
                                    adc.setIntegral(adc_corrected);
                                    adc.setTimeWord(this.extendedFitter.t0);
                                    adc.setPedestal((short) this.extendedFitter.ped);  
//                                    if(table.equals("RF")&&data.getDescriptor().getType().getName().equals("RF")) 
//                                        LOGGER.debug(" FITTING PULSE " + 
//                                                        crate + " / " + slot + " / " + channel
//                                                + " " + nsa + " " + nsb + " " + tet 
//                                                + " " + extendedFitter.adc + " " + extendedFitter.ped*(nsa+nsb)
//                                                + " " + adc.getPedestal() + " " + adc.getADC() + " " + adc.getTime()
//                                    );
                                }
                            }
                        }
                        //LOGGER.debug(" apply nsa nsb " + nsa + " " + nsb);
                        if(data.getADCSize()>0){
                            for(int i = 0; i < data.getADCSize(); i++){
                                    data.getADCData(i).setADC(nsa, nsb);
                            }
                        }
                    }
                }
            }
        }
    }
}
