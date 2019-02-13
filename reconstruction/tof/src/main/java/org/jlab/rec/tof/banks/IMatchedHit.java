package org.jlab.rec.tof.banks;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jlab.utils.groups.IndexedTable;

public interface IMatchedHit {
    Logger LOGGER = LogManager.getLogger(IMatchedHit.class.getName());
    public String DetectorName();

    public List<BaseHit> MatchHits(ArrayList<BaseHit> ADCandTDCLists, double timeJitter, IndexedTable tdcConv, IndexedTable ADCandTDCOffsets);

}
