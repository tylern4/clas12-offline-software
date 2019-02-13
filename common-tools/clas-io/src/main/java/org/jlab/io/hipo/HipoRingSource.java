/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jlab.coda.xmsg.core.xMsg;
import org.jlab.coda.xmsg.core.xMsgCallBack;
import org.jlab.coda.xmsg.core.xMsgConstants;
import org.jlab.coda.xmsg.core.xMsgMessage;
import org.jlab.coda.xmsg.core.xMsgTopic;
import org.jlab.coda.xmsg.data.xMsgRegInfo;
import org.jlab.coda.xmsg.excp.xMsgException;
import org.jlab.coda.xmsg.net.xMsgProxyAddress;
import org.jlab.coda.xmsg.net.xMsgRegAddress;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;
import org.jlab.io.base.DataSourceType;
import org.jlab.jnp.hipo.schema.SchemaFactory;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class HipoRingSource implements DataSource {

    private List<HipoDataEvent>  eventStore = new ArrayList<HipoDataEvent>();
    private int       eventStoreMaxCapacity = 500;
    private SchemaFactory        dictionary = new SchemaFactory();
    private xMsg                 xmsgServer = null;



    public HipoRingSource(String host){
        /*
        super("DataSource",
                new xMsgProxyAddress(host, xMsgConstants.DEFAULT_PORT),
                new xMsgRegAddress(host, xMsgConstants.REGISTRAR_PORT),
                2);
                final String domain  = "clas12-domain";
        final String subject = "clas12-data";
        final String type    = "data";
        final String description = "clas12 data distribution ring";

        xMsgTopic topic = xMsgTopic.build(domain, subject, type);

        try {
            // Register this subscriber
            register(xMsgRegInfo.subscriber(topic, description));
        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }

        try {
            // Subscribe to default proxy
            subscribe(topic, new MyCallBack());
        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }
        System.out.printf("Subscribed to = %s%n", topic);

        this.dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        */
        dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
    }

    public HipoRingSource(){
        dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
    }

    public static HipoRingSource  createSourceDaq(){
        String daqHosts = "129.57.167.107:129.57.167.109:129.57.167.226:129.57.167.227:129.57.167.41:129.57.167.60:129.57.68.135";
        HipoRingSource  reader = new HipoRingSource();
        reader.open(daqHosts);
        return reader;
    }

    public static HipoRingSource createSource(){
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "Complete the sentence:\n"
                    + "\"Green eggs and...\"",
                    "Customized Dialog",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "0.0.0.0");
        if(s!=null){
            LOGGER.debug("----> connecting to host : " + s);
            HipoRingSource source = new HipoRingSource();
            source.open(s);
            LOGGER.debug("\n\n");
            LOGGER.debug("   |---->  caching connection ---> ");
            for(int i = 0; i < 5; i++){
                try {
                    Thread.sleep(1000);
                    LOGGER.debug("   |---->  caching connection ---- " + source.getSize());
                } catch (InterruptedException ex) {
                    LOGGER.error(ex);
                }
            }
            return source;
        }

        return null;
    }

    @Override
    public boolean hasEvent() {
        return (eventStore.size()>0);
    }

    @Override
    public void open(File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean createConnection(String host){
        boolean result = true;
        this.xmsgServer = new xMsg("DataSource",
                new xMsgProxyAddress(host, xMsgConstants.DEFAULT_PORT),
                new xMsgRegAddress(host, xMsgConstants.REGISTRAR_PORT),
                2);
        try {
            if(this.xmsgServer.getConnection()!=null){
                LOGGER.debug("   >>> connection to server " + host + " : success");
            }

        } catch (xMsgException ex) {
            //LOGGER.error(ex);
            LOGGER.debug("   >>> connection to server " + host + " : failed");
            this.xmsgServer.destroy();
            this.xmsgServer = null;
            result = false;
        }
        //LOGGER.debug("-----> connection estabilished...");
        return result;
    }

    @Override
    public void open(String filename) {

        String[] hostList = filename.split(":");

        for(String host : hostList){
            boolean result = this.createConnection(host);
            if(result==true) break;
        }

        if(this.xmsgServer==null){
            LOGGER.debug("----> error finding server.");
            return;
        }
        LOGGER.debug("   >>> subscribing  to topic : data-hipo");

        final String subject = "clas12data";
        final String type    = "data-hipo";
        final String description = "clas12 data distribution ring";
        final String domain  = "clas12domain";
        xMsgTopic topic = xMsgTopic.build(domain, subject, type);
        try {
            // Register this subscriber
            this.xmsgServer.register(xMsgRegInfo.subscriber(topic, description));
        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }

        try {
            // Subscribe to default proxy
            this.xmsgServer.subscribe(topic, new MyCallBack());
        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }
        LOGGER.debug("   >>> subscription to topic : success\n\n");

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void open(ByteBuffer buff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSize() {
        return this.eventStore.size();
    }

    @Override
    public DataEventList getEventList(int start, int stop) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEventList getEventList(int nrecords) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEvent getNextEvent() {
        //LOGGER.debug("   >>> get next event : size = " + eventStore.size());
        if(eventStore.isEmpty()){
            return null;
        }
        HipoDataEvent event = this.eventStore.get(0);
        //LOGGER.debug("   >>> success getting event : size = " + eventStore.size());
        //event.show();
        this.eventStore.remove(0);
        //LOGGER.debug("   >>>   FILO cleanup : size = " + eventStore.size());
        //LOGGER.debug("\n\n");
        return event;
    }

    @Override
    public DataEvent getPreviousEvent() {
        return null;
    }

    @Override
    public DataEvent gotoEvent(int index) {
        return null;
    }

    @Override
    public void reset() {
        this.eventStore.clear();
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }


    public void close() {
        /*try {
            this.getConnection().close();
        } catch (xMsgException ex) {
            LOGGER.error(ex);
        }*/
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.STREAM;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void waitForEvents() {

    }
    private class MyCallBack implements xMsgCallBack {

        @Override
        public void callback(xMsgMessage mm) {
            byte[] data = mm.getData();
            String type = mm.getMimeType();
            //LOGGER.debug("\n\n     >>>>>> received data : mime " + type);
            //LOGGER.debug("     >>>>>> received data : size " + data.length);
            if(eventStore.size()<eventStoreMaxCapacity){
                HipoDataEvent event = new HipoDataEvent(data,dictionary);
                eventStore.add(event);
                //System.out.printf("     >>>>>> adding event to the store : size = %d \n", eventStore.size());
            } else {
                //System.out.printf("     >>>>>> event store is full : size = %d \n", eventStore.size());
            }
        }
    }

    public static void main(String[] args){

        OptionParser parser = new OptionParser();
        parser.addOption("-s", "localhost");
        parser.parse(args);

        HipoRingSource reader = new HipoRingSource();
        reader.open(parser.getOption("-s").stringValue());
        //reader.open("128.82.188.90:129.57.76.220:129.57.76.215:129.57.76.230");

        while(true){
            if(reader.hasEvent()==true){
                //LOGGER.debug("has event");
                DataEvent event = reader.getNextEvent();
                try {
                    event.show();
                } catch (Exception e) {
                    LOGGER.debug("something went wrong");
                }
            } else {
                //LOGGER.debug("no event");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex);
                }
            }
        }
        //reader.open("localhost");
        /*
        OptionParser parser = new OptionParser();
        parser.addRequired("-s");


        HipoRingSource reader = HipoRingSource.createSource();

        while(reader.hasEvent()==true){

            HipoDataEvent  event = (HipoDataEvent) reader.getNextEvent();
            //event.show();

            try {
                Thread.sleep(8000);
            } catch (InterruptedException ex) {
                LOGGER.error(ex);
            }

        }
        LOGGER.debug("DONE");
        */
        /*
        String host = args[0];
        try (HipoRingSource subscriber = new HipoRingSource(host)) {
            xMsgUtil.keepAlive();
        } */
    }
}
