package io.magnum.jetty.server.data.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.codehaus.jackson.annotate.JsonIgnore;

@SuppressWarnings("serial")
public class TimeThroughputMap extends TreeMap<Long, ThroughputRecord> {		
	
    @JsonIgnore
    public List<ThroughputRecord> getRecordList() {
        List<ThroughputRecord> res = new ArrayList<ThroughputRecord>();
        for(Entry<Long, ThroughputRecord> r : this.entrySet()) {
            res.add(r.getValue());
        }        
        return res;
    }
}
