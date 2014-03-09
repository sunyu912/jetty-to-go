package io.magnum.jetty.server;

import io.magnum.jetty.server.data.provider.DataProvider;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class WebController {
	
	private static final String HEALTH_CHECK_API_PATH = "ping";
	
	/** Provider to access and manage all data */
	@SuppressWarnings("unused")
    private DataProvider dataProvider;
	
	@Autowired
	public WebController(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	@RequestMapping(value = "add", method = RequestMethod.GET)
	public void addLocation(			
			HttpServletResponse response) throws Exception {
		
	}
	
	@RequestMapping(value = HEALTH_CHECK_API_PATH, method = RequestMethod.GET)
	public void healthCheck(HttpServletResponse response) throws Exception {	
	    response.setStatus(200);
		response.getWriter().write("success");
	}
}
