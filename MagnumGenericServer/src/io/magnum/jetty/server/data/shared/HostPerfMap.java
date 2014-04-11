package io.magnum.jetty.server.data.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

@SuppressWarnings("serial")
public class HostPerfMap extends HashMap<String, HostPerfRecordTimeMap> {

	@JsonIgnore
	public void putRecordFromRawString(String rawString, Long timestamp, double value) {
		String[] str = rawString.split(" ");
		if (str.length >= 2) {
			String host = str[0];
			String type = str[1];
			
			String dimension = (str.length > 2) ? str[str.length - 1] : null;
			
			// locate the right map record host -> (time->perf) 
			HostPerfRecordTimeMap timeMap = this.get(host);
			if (timeMap == null) {
				timeMap = new HostPerfRecordTimeMap();
			}
			
			// locate the right time map record
			PerfRecord perfRecord = timeMap.get(timestamp);
			if (perfRecord == null) {
				perfRecord = new PerfRecord();
			}
			perfRecord.setValue(type, value, dimension);
			timeMap.put(timestamp, perfRecord);
			
			// finally update the record
			this.put(host, timeMap);
		} else {
			// this should not happen			
		}
	}
	
	@JsonIgnore
	public List<PerfRecord> getPerfList() {
	    for(Map.Entry<String, HostPerfRecordTimeMap> r : this.entrySet()) {
	        return r.getValue().getRecordList();
	    }
	    return null;
	}
	
}
