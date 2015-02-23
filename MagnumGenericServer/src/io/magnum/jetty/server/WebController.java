package io.magnum.jetty.server;

import io.magnum.jetty.server.analyzer.VizwebAnalyzer;
import io.magnum.jetty.server.data.BatchRunHistoryRecord;
import io.magnum.jetty.server.data.BatchRunRecord;
import io.magnum.jetty.server.data.BatchRunResultRecord;
import io.magnum.jetty.server.data.ColorFeatureResult;
import io.magnum.jetty.server.data.QuadtreeFeatureResult;
import io.magnum.jetty.server.data.ScreenshotRecord;
import io.magnum.jetty.server.data.XYFeatureResult;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.screenshot.ScreenshotManager;
import io.magnum.jetty.server.url.JsonMapper;

import java.util.List;

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
	    ScreenshotRecord record = screenshotManager.getScreenshot(url, false);
	    //dataProvider.addScreenshotRecord(record);
	    response.getWriter().write(mapper.writeValueAsString(record));
	}
	
	@RequestMapping(value = "batch/run/add", method = RequestMethod.POST)
    public void addBatchRun(
            @RequestParam(value="description", required=false) String description,
            @RequestParam("urlFile") MultipartFile file,
            HttpServletResponse response) throws Exception {
	    String batchId = screenshotManager.addBatchRun(description, file.getInputStream());
	    response.getWriter().write("{ \"id\" : \"" + batchId + "\"}");
	}
	
	@RequestMapping(value = "batch/run", method = RequestMethod.GET)
    public void batchRun(
            @RequestParam(value="id", required=true) String id,
            HttpServletResponse response) throws Exception {
        BatchRunHistoryRecord r = screenshotManager.batchRun(id);
        response.getWriter().write(JsonMapper.mapper.writeValueAsString(r));
    }
	
	@RequestMapping(value = "list/batch", method = RequestMethod.GET)
    public ModelAndView listBatchRun(
            @RequestParam(value="id", required=false) String id,
            HttpServletResponse response) throws Exception {       
        List<BatchRunRecord> records = dataProvider.listBatchRuns(id);
        ModelAndView modelAndView = new ModelAndView("listBatchRuns");
        modelAndView.addObject("batchRuns", records);        
        return modelAndView;
    }
	
	@RequestMapping(value = "list/batch/history/{id}", method = RequestMethod.GET)
    public ModelAndView listBatchHistory(
            @PathVariable(value = "id") String id,
            HttpServletResponse response) throws Exception {       
        List<BatchRunHistoryRecord> records = dataProvider.listBatchHistory(id);
        ModelAndView modelAndView = new ModelAndView("listBatchHistory");
        modelAndView.addObject("batchHistory", records);        
        modelAndView.addObject("id", id);        
        return modelAndView;
    }
	
	@RequestMapping(value = "list/batch/run/result/{timestamp}", method = RequestMethod.GET)
    public ModelAndView listBatchRunResult(
            @PathVariable(value = "timestamp") Long timestamp,
            HttpServletResponse response) throws Exception {       
        List<BatchRunResultRecord> records = dataProvider.listBatchRunResult(timestamp);
        ModelAndView modelAndView = new ModelAndView("listBatchRunResult");
        modelAndView.addObject("batchRunResult", records);        
        return modelAndView;
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
	    ColorFeatureResult result = vizwebAnalyzer.computeColorFeature(id, null);
	    response.getWriter().write(mapper.writeValueAsString(result));
    }
	
	@RequestMapping(value = "analyze/xy", method = RequestMethod.GET)
    public void analyzerXYFeature(
            @RequestParam("id") String id,
            HttpServletResponse response) throws Exception {       
        XYFeatureResult result = vizwebAnalyzer.computerXYFeature(id, null);
        response.getWriter().write(mapper.writeValueAsString(result));
    }
	
	@RequestMapping(value = "analyze/quadtree", method = RequestMethod.GET)
    public void analyzerQuadtree(
            @RequestParam("id") String id,
            HttpServletResponse response) throws Exception {
        QuadtreeFeatureResult result = vizwebAnalyzer.computerQuadtreeFeature(id, null);
        response.getWriter().write(mapper.writeValueAsString(result));
    }
	
	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public void healthCheck(HttpServletResponse response) throws Exception {	
	    response.setStatus(200);
		response.getWriter().write("success");
	}
}
