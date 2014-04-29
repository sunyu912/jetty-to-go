package io.magnum.jetty.server;

import io.magnum.jetty.server.data.provider.BibleManager;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.data.provider.Sentence;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class WebController {
	
	private static final String HEALTH_CHECK_API_PATH = "ping";
	
	/**
     * Jackson JSON mapper. This might be more convenient to use then
     * SimpleJson.
     */
    private static final ObjectMapper mapper = new ObjectMapper() {{
            configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }};
    
	/** Provider to access and manage all data */
	@SuppressWarnings("unused")
    private DataProvider dataProvider;
	
	@Autowired
	public WebController(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	@RequestMapping(value = "reset", method = RequestMethod.GET)
    public void resetGroup(
            @RequestParam("id") String id,
            HttpServletResponse response) throws Exception {
	    if (id == null) {
	        id = "1";
	    }
	    
        BibleManager.get().updateGroup(id);
        
        response.setStatus(200);
        response.getWriter().write("success");
    }
	
	@RequestMapping(value = "next", method = RequestMethod.GET)
	public void getNextSentenceLocation(HttpServletResponse response) throws Exception {
	    Sentence s = BibleManager.get().getNextSentence();
	    response.setContentType("text/html; charset=utf-8");
	    response.setStatus(200);
        response.getWriter().write(mapper.writeValueAsString(s));
	}
	
	@RequestMapping(value = HEALTH_CHECK_API_PATH, method = RequestMethod.GET)
	public void healthCheck(HttpServletResponse response) throws Exception {	
	    response.setStatus(200);
		response.getWriter().write("success");
	}
}
