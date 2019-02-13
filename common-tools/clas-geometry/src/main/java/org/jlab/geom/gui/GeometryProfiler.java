/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.geom.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.geom.detector.ec.ECFactory;

/**
 *
 * @author gavalian
 */
public class GeometryProfiler {
    public static Logger LOGGER = LogManager.getLogger(GeometryProfiler.class.getName());
    private static final double BYTE_TO_MB = 1024*1024; 
    public static void showHeapUsage(String unit){
        Runtime runtime = Runtime.getRuntime();
        String stats = String.format("MEMORY [%12s] MAX = %15.2f MB   TOTAL = %15.2f MB   FREE = %15.2f MB", unit,
                runtime.maxMemory()/BYTE_TO_MB,runtime.totalMemory()/BYTE_TO_MB,runtime.freeMemory()/BYTE_TO_MB);
        LOGGER.warn(stats);
    }
    public static void main(String[] args){
        GeometryProfiler.showHeapUsage("START");
        ECFactory ecFactory = new ECFactory();
        //Detector ecDetector = ecFactory.createDetectorCLAS();
    }
}
