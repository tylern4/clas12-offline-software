/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.stream;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.coda.jevio.ByteDataTransformer;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioCompactReader;
import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.coda.jevio.EvioReader;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class EvioInputStream {
    public static Logger LOGGER = LogManager.getLogger(EvioInputStream.class.getName());
    private ByteOrder  storeByteOrder = ByteOrder.BIG_ENDIAN;
    private EvioCompactReader evioReader    = null;
    private int        currentEvent;
    private int        currentFileEntries;
    private EvioCompactStructureHandler cStructure = null;

    public void open(String filename){
    try {
            evioReader = new EvioCompactReader(new File(filename));
            currentEvent = 1;
            currentFileEntries = evioReader.getEventCount();
            storeByteOrder     = evioReader.getFileByteOrder();
            LOGGER.debug("****** opened FILE [] ** NEVENTS = " +
                    currentFileEntries + " *******");
            // TODO Auto-generated method stub
        } catch (EvioException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    public Map<Integer,String> getKeys(){
        Map<Integer,String> keymap = new HashMap<Integer,String>();
        return keymap;
    }

    public int getEntries() { return this.currentFileEntries; }

    public TreeMap<Integer,Object>  getObjectFromNode(EvioNode root){
        TreeMap<Integer,Object> treemap = new TreeMap<Integer,Object>();

        List<EvioNode>  nodes = root.getAllNodes();
        for(int loop = 0; loop < nodes.size(); loop++){

            if(nodes.get(loop).getDataTypeObj()==DataType.CHAR8){
                byte[] data = ByteDataTransformer.toByteArray(nodes.get(loop).getByteData(true));
                treemap.put(nodes.get(loop).getNum(),
                        data
                );
            }
            if(nodes.get(loop).getDataTypeObj()==DataType.DOUBLE64){
                double[] data = ByteDataTransformer.toDoubleArray(nodes.get(loop).getByteData(true));
                treemap.put(nodes.get(loop).getNum(),
                        data
                );
            }
             if(nodes.get(loop).getDataTypeObj()==DataType.INT32){
                int[] data = ByteDataTransformer.toIntArray(nodes.get(loop).getByteData(true));
                treemap.put(nodes.get(loop).getNum(),
                        data
                );
            }
        }
        return treemap;
    }

    public TreeMap<Integer,Object> getObjectTree(int event){
        try {
            ByteBuffer evioBuffer = evioReader.getEventBuffer(event, true);
            EvioCompactStructureHandler structure = new EvioCompactStructureHandler(evioBuffer,DataType.BANK);
            List<EvioNode> nodes   = structure.getNodes();
            if(nodes==null) return null;
            for (EvioNode node : nodes) {
                    //LOGGER.warn("--- adding node --");
                    if(node.getTag()==200&&
                            (node.getDataTypeObj()==DataType.ALSOBANK||
                            node.getDataTypeObj()==DataType.BANK)){
                        TreeMap<Integer,Object> objects = this.getObjectFromNode(node);
                        return objects;
                        //if(objects.size()>0) objectArray.add(objects);
                    }
                }
        } catch (EvioException ex) {
            LOGGER.error(ex);
        }
        return null;
    }

    public ArrayList< TreeMap<Integer,Object> > getObjectTree(){
        ArrayList< TreeMap<Integer,Object> > objectArray = new ArrayList< TreeMap<Integer,Object> >();
        for(int loop = 0; loop < currentFileEntries; loop++){
            //LOGGER.warn("--- reading file ---");
            try {
                ByteBuffer evioBuffer = evioReader.getEventBuffer(loop+1, true);
                EvioCompactStructureHandler structure = new EvioCompactStructureHandler(evioBuffer,DataType.BANK);
                List<EvioNode> nodes   = structure.getNodes();
                if(nodes==null) continue;
                for (EvioNode node : nodes) {
                    //LOGGER.warn("--- adding node --");
                    if(node.getTag()==200&&
                            (node.getDataTypeObj()==DataType.ALSOBANK||
                            node.getDataTypeObj()==DataType.BANK)){
                        TreeMap<Integer,Object> objects = this.getObjectFromNode(node);
                        if(objects.size()>0) objectArray.add(objects);
                    }
                }
            } catch (EvioException ex) {
                LOGGER.error(ex);
            }
        }
        return objectArray;
    }

    public void close(){
        this.evioReader.close();
    }
    public ArrayList<Integer> getContainerTags(){
        ArrayList<Integer> tags = new ArrayList<Integer>();
        for(int loop = 0; loop < currentFileEntries; loop++){
            try {
                ByteBuffer evioBuffer = evioReader.getEventBuffer(loop+1, true);
                EvioCompactStructureHandler structure = new EvioCompactStructureHandler(evioBuffer,DataType.BANK);
                List<EvioNode> nodes   = structure.getNodes();
                for(EvioNode item: nodes){
                    if(item.getNum()==0&&
                            item.getDataTypeObj()==DataType.BANK||
                            item.getDataTypeObj()==DataType.ALSOBANK)
                        tags.add(item.getTag());
                    //return item;
                }
            } catch (EvioException ex) {
                LOGGER.error(ex);
            }
        }
        return tags;
    }

    public void readEvent(int event){
        try {
            ByteBuffer evioBuffer = evioReader.getEventBuffer(event, true);
            cStructure = new EvioCompactStructureHandler(evioBuffer,DataType.BANK);
        } catch (EvioException ex) {
            LOGGER.error(ex);
        }

    }

    public EvioNode getNodeFromTree(int tag, int num, DataType type){
        try {
            List<EvioNode> nodes   = cStructure.getNodes();
            if(nodes==null) return null;
            for(EvioNode item: nodes){
                if(type==DataType.INT32){
                    if(item.getTag()==tag&&item.getNum()==num&&
                            (item.getDataTypeObj()==DataType.INT32||item.getDataTypeObj()==DataType.UINT32))
                        return item;
                } else {
                    if(item.getTag()==tag&&item.getNum()==num&&
                            item.getDataTypeObj()==type)
                        return item;
                }
                /*
                if(item.getTag()==tag&&item.getNum()==num&&
                item.getDataTypeObj()==type)
                return item;*/
            }
        } catch (EvioException ex) {
            LOGGER.warn("**** ERROR ***** : error getting node [" + tag
                    + " , " + num + "]  TYPE = " + type);
        }
        return null;
    }

    public byte[] getByte(int tag, int num){
        EvioNode node = this.getNodeFromTree(tag,num,DataType.CHAR8);
        if(node!=null){
            try {
                ByteBuffer buffer = cStructure.getData(node);
                byte[] nodedata = ByteDataTransformer.toByteArray(buffer);
                return nodedata;
            } catch (EvioException ex) {
                LOGGER.error(ex);
            }
        }
        //byte[] ret = {0};
        return null;
    }
    public double[] getDouble(int tag, int num){
        EvioNode node = this.getNodeFromTree(tag,num,DataType.DOUBLE64);
        if(node!=null){
            try {
                ByteBuffer buffer = cStructure.getData(node);
                double[] nodedata = ByteDataTransformer.toDoubleArray(buffer);
                return nodedata;
            } catch (EvioException ex) {
                LOGGER.error(ex);
            }
        }
        //double[] ret = {0.0};
        return null;
    }

    public int[] getInt(int tag, int num) {
        EvioNode node = this.getNodeFromTree(tag, num, DataType.INT32);
        if(node!=null){
            try {
                ByteBuffer buffer = cStructure.getData(node);
                int[] nodedata = ByteDataTransformer.toIntArray(buffer);
                return nodedata;
            } catch (EvioException ex) {
                LOGGER.error(ex);
            }
        }
        return null;
    }
}
