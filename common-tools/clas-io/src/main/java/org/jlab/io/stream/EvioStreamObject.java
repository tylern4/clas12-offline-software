/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public interface EvioStreamObject {
    Logger LOGGER = LogManager.getLogger(EvioStreamObject.class.getName());
    int  getType();
    TreeMap<Integer,Object> getStreamData();
    void setStreamData(TreeMap<Integer,Object> data);
}
