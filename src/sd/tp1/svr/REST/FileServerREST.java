package sd.tp1.svr.REST;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd.tp1.svr.SOAP.FileServerSOAP;

import javax.ws.rs.core.UriBuilder;
import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;

/**
 * Created by franciscorodrigues on 05/04/16.
 */
public class FileServerREST {

    private static String path = "./FileServer";
    private static MulticastSocket server_socket = null;
    private static InetAddress server_address = null;
    private static int server_port = 9000;

    public static void main(String[] args) throws Exception {

        String address_s = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + ":8090" + "/FileServerREST";
        System.err.println("FileServerREST: Started @ " + address_s);
        URI baseUri = UriBuilder.fromUri("http://" + InetAddress.getLocalHost().getCanonicalHostName() + "/FileServerREST").port(8090).build();

        ResourceConfig config = new ResourceConfig();

        config.register(SharedGalleryResource.class);

        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        System.err.println("REST Server ready... ");

        //Create a multicast socket
        server_address = InetAddress.getByName("224.1.2.3");
        if (!server_address.isMulticastAddress()) {
            System.out.println("The address is not multicast!");
            System.exit(1);
        }
        server_socket = new MulticastSocket(server_port);

        // Join a multicast group
        server_socket.joinGroup(server_address);

        // Receives
        Thread r = new Thread(new Runnable() {
            @Override
            public void run() {
                System.err.println("RECEIVING THREAD STARTED!");
                while(true) {
                    try {
                        byte [] buffer = new byte[65536];
                        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                        server_socket.receive(incoming);
                        if(incoming.getLength() > 0) {
                            String incoming_message = new String(incoming.getData(), 0, incoming.getLength());
                            System.out.println("Request received: " + incoming_message);
                            if (incoming_message.contains("FileServer"))
                                sendIPToClient(address_s, incoming.getAddress(), incoming.getPort());
                        }
                        else
                            Thread.sleep(5000);
                    }
                    catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        r.start();
    }

    private static void sendIPToClient(String reply, InetAddress client_address, int client_port) {
        DatagramPacket p = new DatagramPacket(reply.getBytes(), reply.getBytes().length, client_address, client_port);
        try {
            server_socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}



