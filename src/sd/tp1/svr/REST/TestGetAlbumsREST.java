package sd.tp1.svr.REST;

import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * Created by franciscorodrigues on 05/04/16.
 */

public class TestGetAlbumsREST {

    public static void main(String[] args) throws IOException {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(getBaseURI());

        byte[] bytes = target.path("/albums").request().accept(MediaType.APPLICATION_OCTET_STREAM).get(byte[].class);

        System.out.write( bytes );
        System.out.println();
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8090/").build();
    }

}
