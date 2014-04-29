package io.magnum.jetty.server.data.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleManager {

    private static List<String> GROUP1 = Collections.unmodifiableList(new ArrayList<String>() {{
       add("我們日用的飲食，天天賜給我們。");
       add("每早晨這都是新的，你的誠實極其廣大。");
       add("你們存心不可貪愛錢財，要以自己所有的為足；因為主曾說。");
       add("我將這些事告訴你們，是要叫你們在我裡面有平安。在世上你們有苦難，但你們可以放心，我已經勝了世界。");
    }});

    private static List<String> GROUP2 = Collections.unmodifiableList(new ArrayList<String>() {{
        add("帝必聆听您的祷告，他必不撇下您");
        add("这是上帝拯救世人脱离罪恶最痛苦的一幕。");
        add("耶稣没有陷入这批人引起的苦难之中。");
        add("患难最好的态度，就是全心全力爱上帝");
    }});
    
    private static Map<String, List<String>> GROUP_MAP = Collections.unmodifiableMap(new HashMap<String, List<String>>(){{
        put("1", GROUP1);
        put("2", GROUP2);
    }});
        
    private String currentGroupId = null;
    private int currentIndex;
    
    private static BibleManager INSTANCE;    
    
    /**
     * Singletons
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
    }
    
    public synchronized Sentence getNextSentence() {
        List<String> currentGroup = GROUP_MAP.get(currentGroupId);
        if (currentGroup != null && currentIndex < currentGroup.size()) {
            Sentence res = new Sentence();
            res.setIndex(currentIndex);
            res.setSentence(currentGroup.get(currentIndex));
            currentIndex++;
            return res;
        }
        return null;
    }
}
