/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author gavalian
 * @author kenjo
 */
public class CLASResources {
    public static Logger LOGGER = LogManager.getLogger(CLASResources.class.getName());
    public static String getResourcePath(String resource){
        String CLAS12DIR = System.getenv("CLAS12DIR");
        String CLAS12DIRPROP = System.getProperty("CLAS12DIR");
        if(CLAS12DIR!=null){
            return CLAS12DIR + "/" + resource;
        } else {
            LOGGER.warn("[getResourcePath]---> warning the system "
                + " environment CLAS12DIR is not set.");
            if(CLAS12DIRPROP!=null){
                return CLAS12DIRPROP + "/" + resource;
            } else {
                LOGGER.warn("[getResourcePath]---> warning the system "
                + " property CLAS12DIR is not set.");
            }
        }
        
        return null;
    }

    public static String getEnvironmentVariable(String envvarname){
        String envvar = System.getenv(envvarname);
        if(envvar!=null){
            return envvar;
        } else {
            LOGGER.warn("[getEnvironmentVariable]---> warning the system "
                + " environment " + envvarname + " is not set.");
        	  envvar = System.getProperty(envvarname);
            if(envvar!=null){
                return envvar;
            } else {
                LOGGER.warn("[getEnvironmentVariable]---> warning the system "
                + " property " + envvarname + " is not set.");
            }
        }
        
        return null;
    }
}
