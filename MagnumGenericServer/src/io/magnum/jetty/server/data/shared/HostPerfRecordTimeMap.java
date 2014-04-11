package io.magnum.jetty.server.data.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;

@SuppressWarnings("serial")
public class HostPerfRecordTimeMap extends TreeMap<Long, PerfRecord> {		
	
    @JsonIgnore
    public List<PerfRecord> getRecordList() {
        List<PerfRecord> res = new ArrayList<PerfRecord>();
        for(Entry<Long, PerfRecord> r : this.entrySet()) {
            res.add(r.getValue());
        }        
        return res;
    }
}
