package sd.tp1.svr;

import com.sun.xml.internal.ws.wsdl.parser.InaccessibleWSDLException;
import sd.tp1.clt.ws.SharedGalleryServerSOAP;
import sd.tp1.clt.ws.SharedGalleryServerSOAPService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by franciscorodrigues on 22/05/16.
 */
public class SyncSOAP implements Sync {

    private SharedGalleryServerSOAP server; // Stub from the SOAP server
    private String url; // Url of the SOAP server
    private String local_password;

    public SyncSOAP(String url, String password) {
        this.url = url;
        this.local_password = password;
        try {
            server = new SharedGalleryServerSOAPService(new URL(url)).getSharedGalleryServerSOAPPort();
        } catch (MalformedURLException | InaccessibleWSDLException e) {
            System.out.println("CLIENT ERROR: BAD URL or INACCESSIBLE");
        }
    }

    @Override
    public List<String> sync() {
        return server.sendMetadata(local_password);
    }

    @Override
    public byte[] getPictureData(String album, String picture) {
        return server.getPictureData(album, picture, local_password);
    }

}
