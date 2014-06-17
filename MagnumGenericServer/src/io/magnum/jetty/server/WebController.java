package io.magnum.jetty.server;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.CostData;
import io.magnum.jetty.server.data.ResourceAllocation;
import io.magnum.jetty.server.data.RunTestResponse;
import io.magnum.jetty.server.data.analysis.BinPacker;
import io.magnum.jetty.server.data.analysis.BinPackerImpl1;
import io.magnum.jetty.server.data.analysis.BinPackerImpl2;
import io.magnum.jetty.server.data.analysis.BinPackerImpl3;
import io.magnum.jetty.server.data.analysis.BinPackerImpl4;
import io.magnum.jetty.server.data.analysis.CostAnalyzer;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.loadtest.LoadTestManager;
import io.magnum.jetty.server.util.JsonMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.transform.JsonUnmarshallerContext;


@Controller
public class WebController {	

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    
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
            @RequestParam(value="isCotest", required=false) boolean isCotest,
            @RequestParam("testPlan") MultipartFile imagefile,
            HttpServletResponse response) throws Exception {

        logger.info("Start test {} for containerId {} at instance type {} isCotest {}",
                testId, containerId, instanceType, isCotest);
        String id = loadTestManager.runTest(testId, imagefile.getInputStream(), containerId, instanceType, isCotest);
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
            @RequestParam(value="isCotest", required=false) boolean isCotest,
            HttpServletResponse response) throws Exception {
        
        loadTestManager.postProcessingData(testId, containerId, instanceType, isCotest);
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

    @RequestMapping(value = "random/solution", method = RequestMethod.GET)
    public void getRandomSolution(
            HttpServletResponse response) throws Exception {
        
        List<CostData> listCDs = new ArrayList<CostData>();
        
        double sum1 = 0, sum2 = 0, sum3 = 0, sum0 = 0;
        
        for(int i = 0; i < 100; i++) {
            Random r = new Random();
            
            ArrayList<ApplicationCandidate> candidates = new ArrayList<ApplicationCandidate>();
            
            ApplicationCandidate ac1 = new ApplicationCandidate();
            ac1.setContainerId("sunyu912/bm-1-jetty-servlet");
            ac1.setTargetLatency(100d);
            ac1.setTargetThroughput(r.nextInt(47000) + 3000);
            
            ApplicationCandidate ac2 = new ApplicationCandidate();
            ac2.setContainerId("sunyu912/bm-2-go");
            ac2.setTargetLatency(100d);
            ac2.setTargetThroughput(r.nextInt(47000) + 3000);
            
            ApplicationCandidate ac3 = new ApplicationCandidate();
            ac3.setContainerId("sunyu912/bm-3-nodejs");
            ac3.setTargetLatency(100d);
            ac3.setTargetThroughput(r.nextInt(47000) + 3000);
            
            ApplicationCandidate ac4 = new ApplicationCandidate();
            ac4.setContainerId("sunyu912/bm-7-netty");
            ac4.setTargetLatency(100d);
            ac4.setTargetThroughput(r.nextInt(47000) + 3000);
            
            ApplicationCandidate ac5 = new ApplicationCandidate();
            ac5.setContainerId("sunyu912/bm-9-tornado");
            ac5.setTargetLatency(100d);
            ac5.setTargetThroughput(r.nextInt(47000) + 3000);
            
            candidates.add(ac1);
            candidates.add(ac2);
            candidates.add(ac3);
            candidates.add(ac4);
            candidates.add(ac5);
            
            //System.out.println(candidates);
        
        
            List<List<AppPerformanceRecord>> individualPeaks = new ArrayList<List<AppPerformanceRecord>>();
            List<ResourceAllocation> individualRAs = new ArrayList<ResourceAllocation>();
    
            ArrayList<ApplicationCandidate> input1 = new ArrayList<ApplicationCandidate>();
            ArrayList<ApplicationCandidate> input2 = new ArrayList<ApplicationCandidate>();
            ArrayList<ApplicationCandidate> input3 = new ArrayList<ApplicationCandidate>();
    
            double totalIdealCost = 0;
            double totalWorstCost = 0;
            double totalOptPraCost = 0;
            for(ApplicationCandidate a1 : candidates) {                        
                input1.add(a1.clone());
                input2.add(a1.clone());
                input3.add(a1.clone());
    
                ResourceAllocation ra1 = costAnalyzer.getFinalSolution(a1);
                individualRAs.add(ra1);
                totalOptPraCost += ra1.getTotalCost();
                
                List<AppPerformanceRecord> peaks = costAnalyzer.listCost(a1.getContainerId(), a1.getTargetThroughput(), a1.getTargetLatency()); 
                individualPeaks.add(peaks);
                if (peaks != null && peaks.size() > 0) {
                    totalIdealCost += peaks.get(0).getCostAtPeak() * a1.getTargetThroughput();
                    AppPerformanceRecord lastRecord = null;
                    for(int j = peaks.size() - 1; j >= 0; j--) {
                        if (peaks.get(j).getPeakRecord() != null) {
                            lastRecord = peaks.get(j);
                            break;
                        }
                    }
                    if (lastRecord != null) {
                        totalWorstCost += lastRecord.getCostAtPeak() * a1.getTargetThroughput();
                    }
                }
            }                
            
            BinPacker binPacker1 = new BinPackerImpl1(dataProvider, false);
            BinPacker binPacker2 = new BinPackerImpl2(dataProvider, false);
            BinPacker binPacker3 = new BinPackerImpl3(dataProvider, false);        
            
            ResourceAllocation s1 = costAnalyzer.binPacking(binPacker1, input1, false);
            ResourceAllocation s2 = costAnalyzer.binPacking(binPacker2, input2, false);
            ResourceAllocation s3 = costAnalyzer.binPacking(binPacker3, input3, false);
            
            System.out.println("Ideal: " + totalIdealCost);
            System.out.println("Worst: " + totalWorstCost);
            System.out.println("Pract: " + totalOptPraCost);
            System.out.println("S1: " + s1.getTotalCost());
            System.out.println("S2: " + s2.getTotalCost());
            System.out.println("S3: " + s3.getTotalCost());
            
            CostData cd = new CostData(totalIdealCost, totalWorstCost, totalOptPraCost, s1.getTotalCost(), s2.getTotalCost(), s3.getTotalCost());
            listCDs.add(cd);
            
            sum1 += cd.getS1Rate();
            sum2 += cd.getS2Rate();
            sum3 += cd.getS3Rate();
            sum0 += cd.getPracticalRate();
        }
        
        System.out.println(sum0 / 100 + " " + sum1 / 100 + " " + sum2 / 100 + " " + sum3 / 100);
        
        JsonMapper.mapper.writeValue(new File("/tmp/cost.json"), listCDs);
    }
    
    @RequestMapping(value = "packing/solution", method = RequestMethod.GET)
    public ModelAndView getPackingSolution(
            @RequestParam("candidate") String[] candidateStrs,
            @RequestParam(value="enableCotest", required=false) boolean enableCotest,
            HttpServletResponse response) throws Exception {  
        
        List<ResourceAllocation> individualRAs = new ArrayList<ResourceAllocation>();
        List<List<AppPerformanceRecord>> individualPeaks = new ArrayList<List<AppPerformanceRecord>>();
        ArrayList<ApplicationCandidate> candidates = new ArrayList<ApplicationCandidate>();
        ArrayList<ApplicationCandidate> input1 = new ArrayList<ApplicationCandidate>();
        ArrayList<ApplicationCandidate> input2 = new ArrayList<ApplicationCandidate>();
        ArrayList<ApplicationCandidate> input3 = new ArrayList<ApplicationCandidate>();
        ArrayList<ApplicationCandidate> input4 = new ArrayList<ApplicationCandidate>();
        double totalIdealCost = 0;
        double totalWorstCost = 0;
        for(String candidateStr : candidateStrs) {
            String[] args = candidateStr.split("@");
            ApplicationCandidate a1 = new ApplicationCandidate();
            a1.setContainerId(args[0]);
            a1.setTargetThroughput(Integer.parseInt(args[1]));
            a1.setTargetLatency(Double.parseDouble(args[2]));
            candidates.add(a1);
            input1.add(a1.clone());
            input2.add(a1.clone());
            input3.add(a1.clone());
            input4.add(a1.clone());
            individualRAs.add(costAnalyzer.getFinalSolution(a1));
            List<AppPerformanceRecord> peaks = costAnalyzer.listCost(args[0], Integer.parseInt(args[1]), Double.parseDouble(args[2])); 
            individualPeaks.add(peaks);
            if (peaks != null && peaks.size() > 0) {
                totalIdealCost += peaks.get(0).getCostAtPeak() * a1.getTargetThroughput();
                AppPerformanceRecord lastRecord = null;
                for(int j = peaks.size() - 1; j >= 0; j--) {
                    if (peaks.get(j).getPeakRecord() != null) {
                        lastRecord = peaks.get(j);
                        break;
                    }
                }
                if (lastRecord != null) {
                    totalWorstCost += lastRecord.getCostAtPeak() * a1.getTargetThroughput();
                }
            }
        }                
        
        BinPacker binPacker1 = new BinPackerImpl1(dataProvider, enableCotest);
        BinPacker binPacker2 = new BinPackerImpl2(dataProvider, enableCotest);
        BinPacker binPacker3 = new BinPackerImpl3(dataProvider, enableCotest);
        BinPacker binPacker4 = new BinPackerImpl4(dataProvider, enableCotest);
        
        List<ResourceAllocation> ras = new ArrayList<ResourceAllocation>();
        //ras.add(costAnalyzer.binPacking(input1, enableCotest));
        ras.add(costAnalyzer.binPacking(binPacker1, input1, enableCotest));
        ras.add(costAnalyzer.binPacking(binPacker2, input2, enableCotest));
        ras.add(costAnalyzer.binPacking(binPacker3, input3, enableCotest));
        ras.add(costAnalyzer.binPacking(binPacker4, input4, enableCotest));
        
        ModelAndView modelAndView = new ModelAndView("solution_viewer");
        modelAndView.addObject("individualSolutions", individualRAs);
        modelAndView.addObject("individualPeaks", individualPeaks);
        modelAndView.addObject("totalIdealCost", (int) totalIdealCost);
        modelAndView.addObject("totalWorstCost", (int) totalWorstCost);
        //modelAndView.addObject("solution", costAnalyzer.applicationsBinPacking(candidates));
        modelAndView.addObject("solutions", ras);
        return modelAndView;
    }

    @RequestMapping(value = "test/run/get", method = RequestMethod.GET)
    public void getTestRunBenchmarkRecord(
            @RequestParam(value="id", required=true) String id,
            @RequestParam(value="timestamp", required=true) Long timestamp,
            HttpServletResponse response) throws Exception {        
        response.getWriter().write(
                JsonMapper.mapper.writeValueAsString(dataProvider.getBenchmarkRecord(id, timestamp)));
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
