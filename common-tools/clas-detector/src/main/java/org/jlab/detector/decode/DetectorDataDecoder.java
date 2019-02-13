/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public interface DetectorDataDecoder {
    Logger LOGGER = LogManager.getLogger(DetectorDataDecoder.class.getName());
    public List<DetectorDataDgtz>  decode(     List<DetectorDataDgtz> dgtzData);
    public List<DataBank>          createBanks(List<DetectorDataDgtz> dgtzData, DataEvent event);
    
}
