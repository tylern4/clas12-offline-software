package org.jlab.io.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface DataDictionary {
	Logger LOGGER = LogManager.getLogger(DataDictionary.class.getName());
	void init(String format);
	String getXML();
	String[] getDescriptorList();
	DataDescriptor getDescriptor(String desc_name);
        DataBank       createBank(String name, int rows);
}
