package us.categorize;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import us.categorize.api.MessageStoreStubImpl;
import us.categorize.api.UserStoreStubImpl;
import us.categorize.naive.NaiveMessageStore;
import us.categorize.naive.NaiveUserStore;

public class NaiveBootstrap {

	public static void main(String[] args) throws Exception {
		Config config = Config.readRelativeConfig();
		
		Configuration.instance().setMessageStore(new NaiveMessageStore());
		Configuration.instance().setUserStore(new NaiveUserStore());
        Server server = new Server(8080);

        ServletContextHandler ctx = 
                new ServletContextHandler(ServletContextHandler.SESSIONS);
                
        ctx.setContextPath("/");
        server.setHandler(ctx);

        ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/v1/*");
        serHol.setInitOrder(1);
        serHol.setInitParameter("jersey.config.server.provider.packages", 
                "us.categorize.server");

        try {
            server.start();
            server.join();
        } catch (Exception ex) {
           ex.printStackTrace();
        } finally {

            server.destroy();
        }
	}

}
