package io.magnum.jetty.server;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.ResourceAllocation;
import io.magnum.jetty.server.data.RunTestResponse;
import io.magnum.jetty.server.data.analysis.CostAnalyzer;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.loadtest.LoadTestManager;

import java.util.ArrayList;
import java.util.List;

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
    private CostAnalyzer costAnalyzer;

    @Autowired
    public WebController(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @RequestMapping(value = "test/run", method = RequestMethod.POST)
    public void runTest(
            @RequestParam(value="testId", required=false) String testId,
            @RequestParam(value="containerId", required=false) String containerId,
            @RequestParam(value="instanceType", required=false) String instanceType,
            @RequestParam("testPlan") MultipartFile imagefile,
            HttpServletResponse response) throws Exception {

        String id = loadTestManager.runTest(testId, imagefile.getInputStream(), containerId, instanceType);
        response.getWriter().write(mapper.writeValueAsString(new RunTestResponse(id)));
    }

    @RequestMapping(value = "test/run/{testId}", method = RequestMethod.GET)
    public void getTestRunResult(
            @PathVariable("testId") String testId,
            HttpServletResponse response) throws Exception {	    
        response.getWriter().write(mapper.writeValueAsString(dataProvider.getTestInfo(testId)));
    }

    @RequestMapping(value = "test/run/process/{testId}", method = RequestMethod.GET)
    public void getTestProcessedRunResult(
            @PathVariable("testId") String testId,
            @RequestParam(value="containerId", required=false) String containerId,
            @RequestParam(value="instanceType", required=false) String instanceType,
            HttpServletResponse response) throws Exception {
        loadTestManager.postProcessingData(testId, containerId, instanceType);
        response.getWriter().write(mapper.writeValueAsString(dataProvider.getTestInfo(testId)));
    }

    @RequestMapping(value = "test/cost", method = RequestMethod.GET)
    public void getCostResult(
            @RequestParam("containerId") String containerId,
            @RequestParam(value="throughput", required=false) Integer throughput,
            @RequestParam(value="latency", required=false) Double latency,
            HttpServletResponse response) throws Exception {

        response.getWriter().write(mapper.writeValueAsString(costAnalyzer.listCost(containerId, throughput, latency)));
    }

    @RequestMapping(value = "test/cost/solution", method = RequestMethod.GET)
    public void getCostSolution(
            @RequestParam("containerId") String containerId,
            @RequestParam(value="throughput", required=false) Integer throughput,
            @RequestParam(value="latency", required=false) Double latency,
            HttpServletResponse response) throws Exception {

        response.getWriter().write(mapper.writeValueAsString(costAnalyzer.getFinalSolution(containerId, throughput, latency)));
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

    @RequestMapping(value = "cost/solution", method = RequestMethod.GET)
    public ModelAndView getSolutionCal(
            @RequestParam("containerId") String containerId,
            @RequestParam(value="throughput", required=false) Integer throughput,
            @RequestParam(value="latency", required=false) Double latency,            
            HttpServletResponse response) throws Exception {                
        ModelAndView modelAndView = new ModelAndView("solution_cal");

        List<AppPerformanceRecord> records = costAnalyzer.listCost(containerId, throughput, latency);
        ResourceAllocation resourceAllocation = costAnalyzer.getFinalSolution(containerId, throughput, latency);

        modelAndView.addObject("throughput", throughput);
        modelAndView.addObject("latency", latency);
        modelAndView.addObject("peakResultList", records);
        modelAndView.addObject("solution", resourceAllocation);
        return modelAndView;
    }

    @RequestMapping(value = "packing/solution", method = RequestMethod.GET)
    public ModelAndView getPackingSolution(
            @RequestParam("candidate") String[] candidateStrs,
            HttpServletResponse response) throws Exception {  
        
        List<ResourceAllocation> individualRAs = new ArrayList<ResourceAllocation>();
        List<ApplicationCandidate> candidates = new ArrayList<ApplicationCandidate>();
        for(String candidateStr : candidateStrs) {
            String[] args = candidateStr.split("@");
            ApplicationCandidate a1 = new ApplicationCandidate();
            a1.setContainerId(args[0]);
            a1.setTargetThroughput(Integer.parseInt(args[1]));
            a1.setTargetLatency(Double.parseDouble(args[2]));
            candidates.add(a1);
            individualRAs.add(costAnalyzer.getFinalSolution(a1));
        }
        
        ModelAndView modelAndView = new ModelAndView("solution_viewer");
        modelAndView.addObject("individualSolutions", individualRAs);
        //modelAndView.addObject("solution", costAnalyzer.applicationsBinPacking(candidates));
        modelAndView.addObject("solution", costAnalyzer.binPacking(candidates));
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
    
    @RequestMapping(value = "optimizer", method = RequestMethod.GET)
    public ModelAndView getSolutionOptimizer(HttpServletResponse response) throws Exception {                
        ModelAndView modelAndView = new ModelAndView("optimizer");
        modelAndView.addObject("apps", dataProvider.getAvailableApps());
        return modelAndView;
    }

    @RequestMapping(value = "ping", method = RequestMethod.GET)
    public void healthCheck(HttpServletResponse response) throws Exception {	
        response.setStatus(200);
        response.getWriter().write("success");
    }
}
