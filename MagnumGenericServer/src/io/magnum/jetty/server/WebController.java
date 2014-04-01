package io.magnum.jetty.server;

import io.magnum.jetty.server.data.AppConfigLoader;
import io.magnum.jetty.server.data.TrendingSites;
import io.magnum.jetty.server.data.provider.DataProvider;

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

	@RequestMapping(value = "ar/site/trending", method = RequestMethod.GET)
	public void getTrendingSites(HttpServletResponse response) throws Exception {
		response.getWriter().write(mapper.writeValueAsString(TrendingSites.getTrendingSites()));
	}
	
	@RequestMapping(value = "ar/app/config", method = RequestMethod.GET)
    public void getConfigurations(HttpServletResponse response) throws Exception {
        response.getWriter().write(mapper.writeValueAsString(AppConfigLoader.getAppConfig()));        
    }
	
	@RequestMapping(value = "test/prime", method = RequestMethod.GET)
    public void getConfigurations(
            @RequestParam("range") int range,
            HttpServletResponse response) throws Exception {
        response.getWriter().write(mapper.writeValueAsString(dataProvider.getPrimeNumbers(range)));        
    }
	
	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public void healthCheck(HttpServletResponse response) throws Exception {	
	    response.setStatus(200);
		response.getWriter().write("success");
	}
}
