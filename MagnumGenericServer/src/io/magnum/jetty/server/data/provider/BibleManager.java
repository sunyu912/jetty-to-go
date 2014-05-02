package io.magnum.jetty.server.data.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleManager {

    private static List<String> GROUP1 = Collections.unmodifiableList(new ArrayList<String>() {{
       add("[彼得前书5:7] 你们要将一切的忧虑卸给神，因为祂顾念你们。");
       add("[腓利比书4:6] 应当一无挂虑，只要凡事借着祷告、祈求，带着感谢，将你们所要的告诉神；神那超越人所能理解的平安，必在基督耶稣里，保卫你们的心怀意念。");
       add("[Matthew 6:34] Therefore do not worry about tomorrow, for tomorrow will worry about itself. Each day has enough trouble of its own.");
       add("请翻译刚才的第三句英文经文 ^-^");
    }});

    private static List<String> GROUP2 = Collections.unmodifiableList(new ArrayList<String>() {{
        add("［约翰福音15:5］我是葡萄树，你们是枝子；常在我里面的，我也常在他里面，这人就多结果子。因为离了我，你们就不能做什么。");
        add("［约翰福音15:8］你们多结果子，我父就因此得荣耀，你们也就是我的门徒了。");
        add("［以弗所书2:10］我们原是他的工作，在基督耶稣里造成的，为要叫我们行善，就是神所预备叫我们行的。");
        add("［歌罗西书1:10］好叫你们行事为人对得起主，凡事蒙他喜悦，在一切善事上结果子，渐渐的多知道神。");
    }});
    
    private static Map<String, List<String>> GROUP_MAP = Collections.unmodifiableMap(new HashMap<String, List<String>>(){{
        put("1", GROUP1);
        put("2", GROUP2);
    }});
        
    private String currentGroupId = null;
    private int currentIndex;
    
    private Map<String, Sentence> sessionTracker;
    
    private static BibleManager INSTANCE;    
    
    /**
     * Singleton
     */
    public static BibleManager get() {
        if (INSTANCE == null) {
            INSTANCE = new BibleManager();
            INSTANCE.updateGroup("1");
        }
        return INSTANCE;
    }
    
    /**
     * Clear the current group, and reset it with the given group. 
     */
    public synchronized void updateGroup(String groupId) {
        currentGroupId = groupId;
        currentIndex = 0;
        // clear session map
        sessionTracker = new HashMap<String, Sentence>();
    }
    
    public synchronized Sentence getNextSentence(String sessionId) {
        List<String> currentGroup = GROUP_MAP.get(currentGroupId);
        if (sessionId != null && sessionTracker.containsKey(sessionId)) {
            return sessionTracker.get(sessionId);
        }
        if (currentGroup != null && currentIndex < currentGroup.size()) {
            Sentence res = new Sentence();
            res.setIndex(currentIndex);
            res.setSentence(currentGroup.get(currentIndex));
            if (sessionId != null) {
                sessionTracker.put(sessionId, res);
            }
            currentIndex++;
            return res;
        }
        return null;
    }
}
