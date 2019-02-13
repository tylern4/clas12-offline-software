/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.detector.decode.DetectorDataDgtz.ADCData;

/**
 *
 * @author gavalian
 */
public interface IFADCFitter {
    Logger LOGGER = LogManager.getLogger(IFADCFitter.class.getName());
    void fit(ADCData data);
}
