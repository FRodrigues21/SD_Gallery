package sd.tp1.svr.REST;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd.tp1.svr.SharedGalleryClientDiscovery;

import javax.ws.rs.core.UriBuilder;
import java.net.InetAddress;
import java.net.URI;

/**
 * Created by franciscorodrigues on 05/04/16.
 */
public class SharedGalleryServerREST {

    public static void main(String[] args) throws Exception {

        String address_s = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8090" + "/FileServerREST";
        System.err.println("FileServerREST: Started @ " + address_s);
        URI baseUri = UriBuilder.fromUri("http://" + InetAddress.getLocalHost().getCanonicalHostName() + "/FileServerREST").port(8090).build();

        ResourceConfig config = new ResourceConfig();

        config.register(SharedGalleryResource.class);

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        System.err.println("REST Server ready... ");

        // Receives
        Thread r = new Thread(new SharedGalleryClientDiscovery(address_s));
        r.start();
    }

}



