package sd.tp1.svr;

import sd.tp1.gui.GalleryContentProvider;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by franciscorodrigues on 19/05/16.
 */
public class SyncREST implements Sync {

    private Client client = null;
    private WebTarget target = null;
    private String url; // Url of the Rest server
    private int tries; // Number of failed tries to make a request/method
    private String local_password;

    private static final int OK = Response.Status.OK.getStatusCode();
    private static final int CREATED = Response.Status.CREATED.getStatusCode();
    private static final int UNAUTHORIZED = Response.Status.UNAUTHORIZED.getStatusCode();
    private static final int NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
    private static final int INTERNAL_ERROR = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

    public SyncREST(String url, String password) {
        this.tries = 0;
        this.url = url;
        this.local_password = password;

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLSv1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        TrustManager[] trustAllCerts = { new InsecureTrustManager() };
        if (sc != null) {
            try {
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }

        client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).sslContext(sc).build();
        target = client.target(getBaseURI(url));
    }

    private static URI getBaseURI(String url) {
        return UriBuilder.fromUri(url).build();
    }

    @Override
    public List<String> sync() {
        Response response  = target.path("/metadata&password="+local_password).request().accept(MediaType.APPLICATION_JSON).get();
        int status = response.getStatus();
        System.out.println("SYNC: " + status);
        if(status == OK)
            return response.readEntity(ArrayList.class);
        else if(status == UNAUTHORIZED)
            System.out.println("CLIENT ERROR: Wrong password!");
        return null;
    }

    @Override
    public int getTries() {
        return 0;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public List<String> getListOfAlbums() {
        return null;
    }

    @Override
    public List<String> getListOfPictures(GalleryContentProvider.Album album) {
        return null;
    }

    @Override
    public byte[] getPictureData(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return new byte[0];
    }

    @Override
    public String createAlbum(String name) {
        return null;
    }

    @Override
    public Boolean deleteAlbum(GalleryContentProvider.Album album) {
        return null;
    }

    @Override
    public String uploadPicture(GalleryContentProvider.Album album, String name, byte[] data) {
        return null;
    }

    @Override
    public Boolean deletePicture(GalleryContentProvider.Album album, GalleryContentProvider.Picture picture) {
        return null;
    }

    private static class InsecureHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class InsecureTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            Arrays.asList( chain ).forEach(i -> {
                System.err.println( "type: " + i.getType() + " from: " + i.getNotBefore() + " to: " + i.getNotAfter() );
            });
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}