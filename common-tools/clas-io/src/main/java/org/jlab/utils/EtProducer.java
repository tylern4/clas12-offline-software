/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.coda.et.EtSystem;
import org.jlab.coda.et.EtSystemOpenConfig;
import org.jlab.coda.et.exception.EtException;
import org.jlab.coda.et.exception.EtTooManyException;

/**
 *
 * @author gavalian
 */
public class EtProducer {
    public static Logger LOGGER = LogManager.getLogger(EtProducer.class.getName());
    public static void main(String[] args){
        try {

            String etFile = args[0];
            String etHost = args[1];
            Integer etPort = 11111;

            EtSystemOpenConfig config = new EtSystemOpenConfig( etFile,etHost,etPort);
            EtSystem sys = new EtSystem(config);
            sys.open();

        } catch (EtException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        } catch (EtTooManyException ex) {
            LOGGER.error(ex);
        }
    }
}
