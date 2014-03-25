package io.magnum.jetty.server;

import io.magnum.jetty.server.data.RunTestResponse;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.loadtest.LoadTestManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


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
    @Autowired
    private DataProvider dataProvider;
	
	@Autowired
	private LoadTestManager loadTestManager;
	
	@Autowired
	public WebController(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	@RequestMapping(value = "test/run", method = RequestMethod.POST)
    public void runTest(
            @RequestParam(value="testId", required=false) String testId,
            @RequestParam("testPlan") MultipartFile imagefile,
            HttpServletResponse response) throws Exception {
        
        String id = loadTestManager.runTest(testId, imagefile.getInputStream());
        response.getWriter().write(mapper.writeValueAsString(new RunTestResponse(id)));
    }
	
	@RequestMapping(value = "test/run/{testId}", method = RequestMethod.GET)
    public void getTestRunResult(
            @PathVariable("testId") String testId,
            HttpServletResponse response) throws Exception {	    
        response.getWriter().write(mapper.writeValueAsString(dataProvider.getTestInfo(testId)));
    }
	
	@RequestMapping(value = "test/run/{testId}/checker", method = RequestMethod.GET)
    public ModelAndView getTestRunChecker(
            @PathVariable("testId") String testId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {	    	    
	    ModelAndView modelAndView = new ModelAndView("test_result");
        modelAndView.addObject("testInfo", dataProvider.getTestInfo(testId));                
        return modelAndView;        
    }
	
	@RequestMapping(value = "test/run/list", method = RequestMethod.GET)
    public ModelAndView getAllTestRunChecker(
            @RequestParam(value="id", required=false) String id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {                
        ModelAndView modelAndView = new ModelAndView("list_test_result");
        modelAndView.addObject("benchmarkRecords", dataProvider.listBenchmarkRecords(id));
        modelAndView.addObject("title", id == null ? "All" : id);
        return modelAndView;
    }
	
	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public void healthCheck(HttpServletResponse response) throws Exception {	
	    response.setStatus(200);
		response.getWriter().write("success");
	}
}
