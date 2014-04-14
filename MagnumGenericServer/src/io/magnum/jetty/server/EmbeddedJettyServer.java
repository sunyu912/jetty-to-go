package io.magnum.jetty.server;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/** 
 * This launcher is used by war file. 
 * 
 *  @author yusun
 */
public class EmbeddedJettyServer {

	public static void main(String[] args) {
    	// setup server port
	    int port = Integer.parseInt(System.getProperty("port", "8080"));
        Server server = new Server(port);

        ProtectionDomain domain = EmbeddedJettyServer.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        System.out.println(location.toExternalForm());
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        webapp.setServer(server);
        webapp.setWar(location.toExternalForm());

        // (Optional) Set the directory the war will extract to.
        // If not set, java.io.tmpdir will be used, which can cause problems
        // if the temp directory gets cleaned periodically.
        // Your build scripts should remove this directory between deployments
        webapp.setTempDirectory(new File("/tmp/"));

        server.setHandler(webapp);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }        
	}
}
