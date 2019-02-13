/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataDictionary;
import org.jlab.jnp.hipo.data.HipoNode;
import org.jlab.jnp.hipo.schema.Schema;
import org.jlab.jnp.hipo.schema.SchemaFactory;


/**
 *
 * @author gavalian
 */
public class HipoDataDictionary implements DataDictionary {
    
    private final Map<String,HipoDataDescriptor>  descriptors = new HashMap<String,HipoDataDescriptor>();
    
    public HipoDataDictionary(){
        
        SchemaFactory factory = new SchemaFactory();
        factory.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        List<Schema> entries = factory.getSchemaList();
        //LOGGER.debug(" schema size = " + entries.size());
        for(Schema sch : entries){
            HipoDataDescriptor desc = new HipoDataDescriptor(sch);
            descriptors.put(desc.getName(), desc);
            //LOGGER.debug("name = " + sch.getName() + "  desc = " + desc.getName());
        }
        LOGGER.debug("  >>>>> loading default dictionary : entries = " + descriptors.size());
    }
    
    @Override
    public void init(String format) {
        LOGGER.debug("---- INITIALIZATION NOT IMPLEMENTED ----");
    }

    @Override
    public String getXML() {
        return "<xml></xml>";
    }

    
    
    @Override
    public String[] getDescriptorList() {
        Set<String>  list = this.descriptors.keySet();
        int counter = 0;
        String[] tokens = new String[list.size()];
        for(String item : list){
            tokens[counter] = item;
            counter++;
        }
        return tokens;
    }

    @Override
    public DataDescriptor getDescriptor(String desc_name) {
        return descriptors.get(desc_name);
    }

    @Override
    public DataBank createBank(String name, int rows) {
        Map<Integer,HipoNode>  map = descriptors.get(name).getSchema().createNodeMap(rows);
        HipoDataBank bank = new HipoDataBank(map,descriptors.get(name).getSchema());
        return bank;
    }
    
    public static void main(String[] args){
        System.setProperty("CLAS12DIR", "/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava");
        HipoDataDictionary dict = new HipoDataDictionary();
        
        String[] list = dict.getDescriptorList();
        for(String item : list){
            LOGGER.debug("---> " + item);
            HipoDataDescriptor desc = (HipoDataDescriptor) dict.getDescriptor(item);
            String[] entries = desc.getEntryList();
            for(int i = 0; i < entries.length; i++){
                LOGGER.debug("\t\t---> " + entries[i]);
            }
        }
    }
}
