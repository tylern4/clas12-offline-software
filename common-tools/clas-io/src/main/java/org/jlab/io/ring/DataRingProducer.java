/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.ring;

import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.coda.xmsg.core.xMsg;
import org.jlab.coda.xmsg.core.xMsgConnection;
import org.jlab.coda.xmsg.core.xMsgMessage;
import org.jlab.coda.xmsg.core.xMsgTopic;
import org.jlab.coda.xmsg.data.xMsgRegInfo;
import org.jlab.coda.xmsg.excp.xMsgException;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class DataRingProducer extends xMsg {
    public static Logger LOGGER = LogManager.getLogger(DataRingProducer.class.getName());
    private xMsgConnection connection;
    private xMsgTopic           topicEvio;
    private xMsgTopic           topicHipo;

    private int              publishDelay = 0;
    private int            publishCounter = 0;
    private boolean        producerDebug  = false;

    public DataRingProducer(){
        super("DataRingProducer");
    }
    /**
     * sets the sleep timer after each event has been published.
     * @param delay
     */
    public void setDelay(int delay){
        this.publishDelay = delay;
    }

    public void updateList(){

    }
    /**
     * Start publisher service. establishes connection
     */
    public void start(){

        try {

            connection = getConnection();

            final String domain   = "clas12domain";
            final String subject  = "clas12data";
            final String typeHipo = "data-hipo";
            final String typeEvio = "data-evio";

            final String description = "clas12 data distribution ring";

            topicHipo = xMsgTopic.build(domain, subject, typeHipo);
            topicEvio = xMsgTopic.build(domain, subject, typeEvio);

            register(xMsgRegInfo.publisher(topicHipo, description));
            //register(xMsgRegInfo.publisher(topicEvio, description));


        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }

        LOGGER.debug("\n\n    >>>>>> success host : " + this.connection.getAddress().host());
        LOGGER.debug("    >>>>>> success port : " + this.connection.getAddress().pubPort());
    }

    public void addEvioEvent(EvioDataEvent event){
        byte[] b = event.getHandler().getStructure().getByteBuffer().array();
        xMsgMessage  msg = new xMsgMessage(topicEvio,"data/evio",b);
        try {
            this.publish(connection, msg);
        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }
        if(this.producerDebug==true){
            LOGGER.debug("\n\n    >>>>>>  published message : # " + this.publishCounter);
            LOGGER.debug("    >>>>>>  published message : size = " + b.length);
            LOGGER.debug("    >>>>>>  delay " + publishDelay + "  ms");
        }
        //this.publishCounter++;
    }


    public void shutdown(){

        this.unsubscribeAll();
        this.connection.close();
        this.destroy();
    }

    public void addEvent(HipoDataEvent event){

        byte[] b = event.getEventBuffer().array();

        xMsgMessage  msg = new xMsgMessage(topicHipo,"data/hipo",b);

        try {
            this.publish(connection, msg);
        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }
        if(this.producerDebug==true){
            LOGGER.debug("\n\n    >>>>>>  published message : # " + this.publishCounter);
            LOGGER.debug("    >>>>>>  published message : size = " + b.length);
            LOGGER.debug("    >>>>>>  delay " + publishDelay + "  ms");
        }
        this.publishCounter++;
    }


    public static void main(String[] args){
        int delay   = Integer.parseInt(args[0]);
        String file = args[1];

        DataRingProducer  producer = new DataRingProducer();
        producer.setDelay(delay);
        producer.start();

        HipoDataSource reader = new HipoDataSource();
        reader.open(file);
        while(reader.hasEvent()==true){
            HipoDataEvent event = (HipoDataEvent) reader.getNextEvent();
            producer.addEvent(event);
        }
    }
}
