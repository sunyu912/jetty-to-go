package io.magnum.jetty.server;

import io.magnum.jetty.server.data.provider.BibleManager;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.data.provider.Sentence;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class WebController {
	
	private static final String HEALTH_CHECK_API_PATH = "ping";
	
	private static Logger logger = LoggerFactory.getLogger(WebController.class);
	
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
            @RequestParam(value = "id", required = false) String id,
            HttpServletResponse response) throws Exception {
	    if (id == null) {
	        id = "1";
	    }
	    
        BibleManager.get().updateGroup(id);
        
        response.setStatus(200);
        response.getWriter().write("success");
    }
	
	@RequestMapping(value = "next", method = RequestMethod.GET)
	public void getNextSentenceLocation(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    logger.info("Request from session ID: " + request.getSession().getId());	    
	    Sentence s = BibleManager.get().getNextSentence(request.getSession().getId());
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
