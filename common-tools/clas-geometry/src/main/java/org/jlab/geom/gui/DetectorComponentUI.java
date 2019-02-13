/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.Polygon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Path3D;

/**
 *
 * @author gavalian
 */
public class DetectorComponentUI {
    public static Logger LOGGER = LogManager.getLogger(DetectorComponentUI.class.getName());
    public int SECTOR = 0;
    public int LAYER  = 0;
    public int COMPONENT = 0;
    public Boolean isActive = false;
    
    public DetectorId  detectorType = DetectorId.UNDEFINED;
    //public Path3D     shapePolygon = new Path3D();
    public Polygon     shapePolygon = new Polygon();
    
    public DetectorComponentUI(){
        
    }
    
    public void setPolygon(Polygon pol){
        this.shapePolygon = pol;
    }
    
    public void show(){
        LOGGER.debug("POLYGON");
        int[] x = this.shapePolygon.xpoints;
        int[] y = this.shapePolygon.ypoints;
        for(int loop = 0; loop < x.length;loop++){
            LOGGER.debug("\t  " + loop + " X/Y : " + x[loop] + " : " + y[loop]);
        }
    }
}
