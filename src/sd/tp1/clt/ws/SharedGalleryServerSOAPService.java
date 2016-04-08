
package sd.tp1.clt.ws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "SharedGalleryServerSOAPService", targetNamespace = "http://SOAP.svr.tp1.sd/", wsdlLocation = "http://10-22-105-88.ed2.eduroam.fct.unl.pt:8080/FileServerSOAP?wsdl")
public class SharedGalleryServerSOAPService
    extends Service
{

    private final static URL SHAREDGALLERYSERVERSOAPSERVICE_WSDL_LOCATION;
    private final static WebServiceException SHAREDGALLERYSERVERSOAPSERVICE_EXCEPTION;
    private final static QName SHAREDGALLERYSERVERSOAPSERVICE_QNAME = new QName("http://SOAP.svr.tp1.sd/", "SharedGalleryServerSOAPService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://10-22-105-88.ed2.eduroam.fct.unl.pt:8080/FileServerSOAP?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        SHAREDGALLERYSERVERSOAPSERVICE_WSDL_LOCATION = url;
        SHAREDGALLERYSERVERSOAPSERVICE_EXCEPTION = e;
    }

    public SharedGalleryServerSOAPService() {
        super(__getWsdlLocation(), SHAREDGALLERYSERVERSOAPSERVICE_QNAME);
    }

    public SharedGalleryServerSOAPService(WebServiceFeature... features) {
        super(__getWsdlLocation(), SHAREDGALLERYSERVERSOAPSERVICE_QNAME, features);
    }

    public SharedGalleryServerSOAPService(URL wsdlLocation) {
        super(wsdlLocation, SHAREDGALLERYSERVERSOAPSERVICE_QNAME);
    }

    public SharedGalleryServerSOAPService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, SHAREDGALLERYSERVERSOAPSERVICE_QNAME, features);
    }

    public SharedGalleryServerSOAPService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SharedGalleryServerSOAPService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns SharedGalleryServerSOAP
     */
    @WebEndpoint(name = "SharedGalleryServerSOAPPort")
    public SharedGalleryServerSOAP getSharedGalleryServerSOAPPort() {
        return super.getPort(new QName("http://SOAP.svr.tp1.sd/", "SharedGalleryServerSOAPPort"), SharedGalleryServerSOAP.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SharedGalleryServerSOAP
     */
    @WebEndpoint(name = "SharedGalleryServerSOAPPort")
    public SharedGalleryServerSOAP getSharedGalleryServerSOAPPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://SOAP.svr.tp1.sd/", "SharedGalleryServerSOAPPort"), SharedGalleryServerSOAP.class, features);
    }

    private static URL __getWsdlLocation() {
        if (SHAREDGALLERYSERVERSOAPSERVICE_EXCEPTION!= null) {
            throw SHAREDGALLERYSERVERSOAPSERVICE_EXCEPTION;
        }
        return SHAREDGALLERYSERVERSOAPSERVICE_WSDL_LOCATION;
    }

}