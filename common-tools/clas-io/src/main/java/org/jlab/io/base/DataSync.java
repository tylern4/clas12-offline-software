/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author gavalian
 */
public interface DataSync {
    Logger LOGGER = LogManager.getLogger(DataBank.class.getName());
    void open(String file);
    void writeEvent(DataEvent event);
    void close();
    
    DataEvent createEvent();
}
