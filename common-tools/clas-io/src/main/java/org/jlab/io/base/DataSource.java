package org.jlab.io.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.ByteBuffer;

public interface DataSource {
    Logger LOGGER = LogManager.getLogger(DataBank.class.getName());
    boolean hasEvent();
    void open(File file);
    void open(String filename);
    void open(ByteBuffer buff);
    void close();
    int getSize();
    void waitForEvents();
    DataEventList getEventList(int start, int stop);
    DataEventList getEventList(int nrecords);
    DataEvent     getNextEvent();
    DataEvent     getPreviousEvent();
    DataEvent     gotoEvent(int index);
    void reset();
    int getCurrentIndex();
    DataSourceType  getType();
}
