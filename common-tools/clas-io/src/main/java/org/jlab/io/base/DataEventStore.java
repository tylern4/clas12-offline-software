/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public class DataEventStore {
    public static Logger LOGGER = LogManager.getLogger(DataEventStore.class.getName());
    private TreeMap<String,DataBank> store = new TreeMap<String,DataBank>();
    
    
    
    public DataEventStore(){
        
    }
    
    public DataEventStore(DataEvent event){
        this.init(event);
    }
    
    public final void init(DataEvent event){
        String[] banks = event.getBankList();
        store.clear();
        for(String bank : banks){
            DataBank  db = event.getBank(bank);
            if(db!=null){
                store.put(bank, db);
            } else {
                LOGGER.warn("[DataEventStore::init] ----> error : reading bank "
                + " [" + bank + "]  failed....");
            }
        }
    }
        
    public DataBank getBank(String name){
        return this.store.get(name);
    }
    
    public boolean hasBank(String name){
        return this.store.containsKey(name);
    }        
    
    public void show(){
        for(Map.Entry<String,DataBank> bank : this.store.entrySet()){
            LOGGER.debug(String.format("| %-24s | %6d | %6d |", bank.getKey(),
                    bank.getValue().rows(),bank.getValue().rows()));
        }
    }
}
