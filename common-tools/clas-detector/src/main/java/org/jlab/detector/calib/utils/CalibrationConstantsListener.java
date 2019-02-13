/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author gavalian
 */
public interface CalibrationConstantsListener {
    Logger LOGGER = LogManager.getLogger(CalibrationConstantsListener.class.getName());
    void constantsEvent(CalibrationConstants cc, int col, int row);
}
