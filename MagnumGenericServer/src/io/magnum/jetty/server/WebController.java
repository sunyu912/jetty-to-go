package io.magnum.jetty.server;

import io.magnum.jetty.server.data.AppConfigLoader;
import io.magnum.jetty.server.data.TrendingSites;
import io.magnum.jetty.server.data.provider.DataProvider;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
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
    public void getPrime(
            @RequestParam("range") int range,
            HttpServletResponse response) throws Exception {
        response.getWriter().write(mapper.writeValueAsString(dataProvider.getPrimeNumbers(range)));        
    }
	
	@RequestMapping(value = "test/largefile", method = RequestMethod.GET)
    public void getLargeFile(
            HttpServletResponse response) throws Exception {
	    File file = new File("/tmp/largefile.dat");
        int length   = 0;
        ServletOutputStream outStream = response.getOutputStream();
//        ServletContext context  = getServletConfig().getServletContext();
//        String mimetype = context.getMimeType(filePath);
        
        // sets response content type
//        if (mimetype == null) {
          String mimetype = "application/octet-stream";
//        }
        response.setContentType(mimetype);
        response.setContentLength((int)file.length());
        String fileName = (new File("/tmp/largefile.dat")).getName();
        
        // sets HTTP header
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        
        byte[] byteBuffer = new byte[4096];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        
        // reads the file's bytes and writes them to the response stream
        while ((in != null) && ((length = in.read(byteBuffer)) != -1))
        {
            outStream.write(byteBuffer,0,length);
        }
        
        in.close();
        outStream.close();        
    }
	
	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public void healthCheck(HttpServletResponse response) throws Exception {	
	    response.setStatus(200);
		response.getWriter().write("success");
	}
}
