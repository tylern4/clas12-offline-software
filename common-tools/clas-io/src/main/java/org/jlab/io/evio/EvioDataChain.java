/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.evio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.coda.jevio.EvioCompactReader;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;
import org.jlab.utils.FileUtils;

/**
 *
 * @author gavalian
 */
public class EvioDataChain  {
    public static Logger LOGGER = LogManager.getLogger(EvioDataEvent.class.getName());
    private ArrayList<String> fileList = new ArrayList<String>();
    private Integer           currentFileNumber = 0;
    private Integer           currentFileNumberOfEvents = 0;
    private Integer           currentFilePosition       = 0;
    private EvioCompactReader reader = null;
    private ByteBuffer        eventBuffer = null;
    private Long              printoutTimeLast = (long) 0;
    private Boolean           printoutEnabled  = true;
    private Integer           readerCount      = 0;
    private double            timeIntervalPrintoutSec = 10.0;

    public EvioDataChain(){
        printoutTimeLast = System.currentTimeMillis();
    }

    public void addDir(String directory){
        this.addDir(directory, "evio");
    }

    public void addDir(String directory, String extension){
        ArrayList<String> list = FileUtils.filesInFolder(new File(directory), extension);
        fileList.clear();
        for(String item : list){
            LOGGER.debug("add : " + item);
            fileList.add(item);
        }
    }

    public void addFile(String filename){
        fileList.add(filename);
    }

    public void open() {
        LOGGER.warn("** OPENNING STREAM WITH FILE # = " + this.fileList.size()
        + " **");

        try {
            reader = new EvioCompactReader(fileList.get(0));
            this.currentFileNumberOfEvents = reader.getEventCount();
            this.currentFilePosition = 1;
            this.currentFileNumber   = 0;
            this.readerCount         = 0;
        } catch (EvioException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    public boolean hasEvent(){
        if(this.currentFileNumber==this.fileList.size()-1){
            if(this.currentFilePosition<this.currentFileNumberOfEvents){
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public EvioDataEvent getNextEvent(){
        this.nextEvent();
        if(EvioFactory.getDictionary()!=null){
            EvioDataEvent event = new EvioDataEvent(this.eventBuffer,EvioFactory.getDictionary());
            return event;
        }
        return new EvioDataEvent(this.eventBuffer);
    }

    public boolean nextEvent() {

        long nextTime = System.currentTimeMillis();
        double elapsedTime = (nextTime-this.printoutTimeLast)*1e-6;
        this.readerCount++;

        if(elapsedTime>this.timeIntervalPrintoutSec){
            this.printoutTimeLast = System.currentTimeMillis();
            LOGGER.debug(String.format("evio reader : %8.1f events %12d ",
                    elapsedTime,readerCount));
        }

        if(this.currentFilePosition<this.currentFileNumberOfEvents){
            try {
                this.eventBuffer = reader.getEventBuffer(this.currentFilePosition);
                this.currentFilePosition++;
                return true;
            } catch (EvioException ex) {
                LOGGER.error(ex);
            }
        } else {
            if(this.currentFileNumber<fileList.size()){
                this.currentFileNumber++;

                try {
                    reader = new EvioCompactReader(fileList.get(this.currentFileNumber));
                    this.currentFileNumberOfEvents = reader.getEventCount();
                    this.currentFilePosition = 1;
                    this.eventBuffer = reader.getEventBuffer(this.currentFilePosition);
                    this.currentFilePosition++;
                    return true;
                } catch (EvioException ex) {
                    LOGGER.error(ex);
                } catch (IOException ex) {
                    LOGGER.error(ex);
                }
            }
        }
        return false;
    }



}
