package io.magnum.jetty.server;

import io.magnum.jetty.server.analyzer.VizwebAnalyzer;
import io.magnum.jetty.server.data.ColorFeatureResult;
import io.magnum.jetty.server.data.QuadtreeFeatureResult;
import io.magnum.jetty.server.data.ScreenshotRecord;
import io.magnum.jetty.server.data.XYFeatureResult;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.screenshot.ScreenshotManager;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    private DataProvider dataProvider;
	
	@Autowired
	private ScreenshotManager screenshotManager;
	@Autowired
	private VizwebAnalyzer vizwebAnalyzer;
	
	@Autowired
	public WebController(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	@RequestMapping(value = "screenshot", method = RequestMethod.GET)
	public void getScreenshot(
	        @RequestParam("url") String url,
	        HttpServletResponse response) throws Exception {
	    ScreenshotRecord record = screenshotManager.getScreenshot(url);
	    dataProvider.addScreenshotRecord(record);
	    response.getWriter().write(mapper.writeValueAsString(record));
	}
	
	@RequestMapping(value = "list/screenshot", method = RequestMethod.GET)
    public ModelAndView listScreenshots(
            @RequestParam(value="url", required=false) String url,
            HttpServletResponse response) throws Exception {	   
        List<ScreenshotRecord> records = dataProvider.listScreenshots(url);
        ModelAndView modelAndView = new ModelAndView("listScreenshots");
        modelAndView.addObject("records", records);        
        return modelAndView;
    }
	
	@RequestMapping(value = "analyze/color", method = RequestMethod.GET)
    public void analyzerColor(
            @RequestParam("id") String id,
            HttpServletResponse response) throws Exception {	    
	    ColorFeatureResult result = vizwebAnalyzer.computeColorFeature(id);
	    response.getWriter().write(mapper.writeValueAsString(result));
    }
	
	@RequestMapping(value = "analyze/xy", method = RequestMethod.GET)
    public void analyzerXYFeature(
            @RequestParam("id") String id,
            HttpServletResponse response) throws Exception {       
        XYFeatureResult result = vizwebAnalyzer.computerXYFeature(id);
        response.getWriter().write(mapper.writeValueAsString(result));
    }
	
	@RequestMapping(value = "analyze/quadtree", method = RequestMethod.GET)
    public void analyzerQuadtree(
            @RequestParam("id") String id,
            HttpServletResponse response) throws Exception {
        QuadtreeFeatureResult result = vizwebAnalyzer.computerQuadtreeFeature(id);
        response.getWriter().write(mapper.writeValueAsString(result));
    }
	
	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public void healthCheck(HttpServletResponse response) throws Exception {	
	    response.setStatus(200);
		response.getWriter().write("success");
	}
}
