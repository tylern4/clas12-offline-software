package org.jlab.io.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DataDescriptor is the dictionary of a single DataBank object.
 * It consists of a list of Entries by name and integer ID.
 */
public interface DataDescriptor {
    Logger LOGGER = LogManager.getLogger(DataDescriptor.class.getName());
	void init(String s);
	String[] getEntryList();
	String getName();
        String getXML();
        boolean  hasEntry(String entry);
        boolean  hasEntries(String... entries);
	int getProperty(String property_name, String entry_name);
	int getProperty(String property_name);
        void    setPropertyString(String name, String value);
        String  getPropertyString(String property_name);
        void show();
}
