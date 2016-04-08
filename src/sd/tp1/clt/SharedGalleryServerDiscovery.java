package sd.tp1.clt;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryServerDiscovery implements Runnable{

    private int client_port = 9000;
    private InetAddress client_address = null;
    private MulticastSocket client_socket = null;

    private Map<String,Request> servers = new ConcurrentHashMap<>();

    public SharedGalleryServerDiscovery() {
    }

    public Map<String,Request> getServers() {
        return servers;
    }

    public void removeServer(String address) {
        servers.remove(address);
    }

    public void run() {

        try {
            client_address = InetAddress.getByName("224.1.2.8");
            client_socket = new MulticastSocket();
        }
        catch (IOException e) {
            System.exit(1);
        }

        // Receives packets with SERVER IP
        Thread r = new Thread(() -> {
            while(true) {
                DatagramPacket reply;
                byte [] buffer = new byte[65536];
                reply = new DatagramPacket(buffer, buffer.length);
                    try {
                        client_socket.setSoTimeout(5000);
                        client_socket.receive(reply);
                        if(reply.getLength() > 0) {
                            String url = new String(reply.getData(), 0, reply.getLength());
                            if(!servers.containsKey(url) && url.contains("http")) {
                                if (url.contains("SOAP")) {
                                    System.err.println("ADDED: " + url);
                                    servers.put(url, new RequestSOAP(url));
                                }
                                else if(url.contains("REST")) {
                                    System.err.println("ADDED: " + url);
                                    servers.put(url, new RequestREST(url));
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("CLIENT ERROR: No packet received!");
                    }
                }
        });
        r.start();

        // Sends packets to Server
        Thread s = new Thread(() -> {
                while (true) {
                    try {
                        DatagramPacket request;
                        String data_req = "FileServer";
                        byte [] data_cont = data_req.getBytes();
                        request = new DatagramPacket(data_cont, data_cont.length, client_address, client_port);
                        client_socket.send(request);
                        Thread.sleep(5000);
                    } catch (IOException | InterruptedException e) {
                        System.err.println("CLIENT ERROR: Could not send packet to servers!");
                    }
                }
        });
        s.start();

    }

}
