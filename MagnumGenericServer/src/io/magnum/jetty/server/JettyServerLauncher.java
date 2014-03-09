package io.magnum.jetty.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServerLauncher {

	/** 
	 * Launch the Bolt server from here  
	 */
	public static void main(String[] args) {
		
		// configure web app context
		WebAppContext context = new WebAppContext();
		context.setDescriptor("war/WEB-INF/web.xml");
		context.setResourceBase("war");
		context.setContextPath("/");
		
		// setup server port
		Server server = new Server(8080);
		server.setHandler(context);
		
		// start the server
        try {
        	server.start();
        	server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
