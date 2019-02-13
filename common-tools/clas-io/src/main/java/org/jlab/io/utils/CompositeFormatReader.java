/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.coda.jevio.CompositeData;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioCompactReader;
import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class CompositeFormatReader {
    public static Logger LOGGER = LogManager.getLogger(CompositeFormatReader.class.getName());
    public static void main4(String[] args){
        EvioSource reader = new EvioSource();
        reader.open("/Users/gavalian/Work/DataSpace/LTCC/ltcc0test_000195.evio");
        //while(reader.hasEvent()){
        for(int loop = 0; loop < 10; loop++){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            EvioNode      rawnode = event.getNodeFromTree(57601, 43, DataType.COMPOSITE);
            if(rawnode!=null){
                LOGGER.warn("FOUND NODE " + rawnode.getTag());
                ByteBuffer buffer = rawnode.getByteData(true);
                try {
                    CompositeData  cdata      = new CompositeData(buffer.array(),event.getByteOrder());
                    List<DataType> cdataTypes = cdata.getTypes();
                    List<Object>   cdataitems = cdata.getItems();
                    LOGGER.warn("DATA = " + cdataTypes.size() + " " + cdataitems.size());
                    for(Object item : cdataitems){
                        if(item instanceof Short){
                            LOGGER.warn(" Found short ");
                        }
                    }
                } catch (EvioException ex) {
                    LOGGER.error(ex);
                }
            }
        }
    }


    public static void main2(){

        try {
            EvioCompactReader reader = new EvioCompactReader("/Users/gavalian/Work/DataSpace/LTCC/ltcc0test_000195.evio");

            for(int loop = 1; loop < 10; loop++){
                EvioNode node = reader.getEvent(loop);
                if(node==null){
                    LOGGER.warn("Node is null ");
                } else {
                    EvioCompactStructureHandler structure = new EvioCompactStructureHandler(node);
                    List<EvioNode> nodes = structure.getNodes();
                    LOGGER.warn("STRUCTREADER = SIZE = " + nodes.size());
                }
            }
        } catch (EvioException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }
    public static void main(String[] args){
        CompositeFormatReader.main4(args);
    }
    public static void main3(String[] args){
        try {
            EvioCompactReader reader = new EvioCompactReader("/Users/gavalian/Work/DataSpace/LTCC/ltcc0test_000195.evio");

            for(int loop = 0; loop < 10; loop++){
                //EvioNode node = reader.getEvent(loop);
                //List<EvioNode> nodes = reader.searchEvent(loop+1, 57601, 7);
                //LOGGER.warn("event " + loop + "  " + nodes.size());
                ByteBuffer buffer = reader.getEventBuffer(loop+1);
                LOGGER.warn("event " + loop + "  " + buffer.limit() + "  " + buffer.order());
                EvioCompactStructureHandler structure = new EvioCompactStructureHandler(buffer,DataType.BANK);
                List<EvioNode> structnodes = structure.getNodes();
                LOGGER.warn("STRUCTURE NODE COUNT = " + structnodes.size());
                EvioNode base = structure.getScannedStructure();
                if(base==null){
                    LOGGER.warn("ERROR node is null");
                } else {
                    ArrayList<EvioNode>  pnodes = base.getAllNodes();
                    ArrayList<EvioNode> nodes = base.getChildNodes();
                    if(nodes!=null){
                        LOGGER.warn("Number of nodes = " + nodes.size() +
                                "  pnodes = " + pnodes.size());
                        for(EvioNode node : nodes){
                            LOGGER.warn("FOUND THE NODE " + node.getTag()
                            + "  " + node.getNum());
                            if(node.getTag()==43){
                                ArrayList<EvioNode> nodes_child = node.getChildNodes();
                                for(EvioNode child : nodes_child){
                                    if(child.getTag()==57601){

                                        ByteBuffer nodeBuffer = child.getByteData(true);
                                        byte[] bdata = nodeBuffer.array();
                                        LOGGER.warn(" -----> FOUND THE NODE " + bdata.length
                                         + " " + child.getType());
                                        System.err.printf("%x %x %x %x %x %x %x %x %x %x\n",
                                                bdata[0],bdata[1],
                                                bdata[2],bdata[3],
                                                bdata[4], bdata[5],
                                                bdata[6], bdata[7],
                                                bdata[8], bdata[9]
                                                );

                                        CompositeData cdata = new CompositeData(bdata,ByteOrder.LITTLE_ENDIAN);
                                        List<DataType> itemTypes = cdata.getTypes();
                                        //String format = cdata.
                                        //for(DataType type : itemTypes){
                                        //    LOGGER.warn(" Data type = " + type);
                                        //}
                                    }
                                }
                            }
                        }
                    }
                }

            }
        } catch (EvioException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }
    /*
    public static void main(String[] args) {
        EvioSource reader = new EvioSource();
        reader.open("/Users/gavalian/Work/DataSpace/LTCC/ltcc0test_000168.evio");
        for(int loop = 0; loop < 10; loop++){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            LOGGER.warn("Event " + loop + "  Has bank = " + event.hasBank(57601, 7));
            EvioCompactStructureHandler structure = event.getStructureHandler();
            try {
                List<EvioNode> nodes = structure.getNodes();
                /*
                if(event.hasBank(57601, 7)==true){

                EvioNode node = event.getNodeFromTree(57601, 7, DataType.COMPOSITE);

                if(node!=null){
                LOGGER.warn("FOUND THE NODE");
                //ByteBuffer  buffer = node.getStructureBuffer(false);

                try {
                CompositeData cdata = new CompositeData(buffer.array(),ByteOrder.LITTLE_ENDIAN);
                } catch (EvioException ex) {
                LOGGER.error(ex);
                }
                }
                }
            } catch (EvioException ex) {
                LOGGER.error(ex);
            }
        }
    }*/
}
