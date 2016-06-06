package sd.tp1.clt;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryServerDiscovery implements Runnable {

    private String client_multicast = "224.1.2.8"; // Multicast IP
    private int client_port = 9000; // Multicast Port
    private InetAddress client_address = null; // Client address
    private MulticastSocket client_socket = null; // Socket Multicast
    private String local_password;
    private SharedGalleryContentProvider provider;

    private Map<String,Request> servers = new ConcurrentHashMap<>(); // Map containing servers where the key is the server address and the value an object that handles the requests

    public SharedGalleryServerDiscovery(String password, SharedGalleryContentProvider provider) {
        local_password = password;
        this.provider = provider;
    }

    /**
     * Get the map of servers
     * @return the map of servers
     */
    public Map<String,Request> getServers() {
        return servers;
    }

    /**
     * Removes server with a certain address
     * @param address - Address of a server
     */
    public void removeServer(String address) {
        servers.remove(address);
        provider.updateAlbums("", "");
    }

    public void run() {

        try {
            client_address = InetAddress.getByName(client_multicast);
            client_socket = new MulticastSocket();
        }
        catch (IOException e) {
            System.exit(1);
        }

        // Receives packets from servers/clients that are a FileServer
        new Thread(() -> {
            for(;;) {
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
                                    servers.put(url, new RequestSOAP(url, local_password));
                                }
                                else if(url.contains("REST")) {
                                    System.err.println("ADDED: " + url);
                                    servers.put(url, new RequestREST(url, local_password));
                                }
                                if(provider != null)
                                    provider.updateAlbums("","");
                            }
                        }
                    } catch (IOException e) {
                        //System.out.println("CLIENT ERROR: No packet received!");
                    }
                }
        }).start();

        // Sends packets to multicast asking who is a FileServer
        new Thread(() -> {
                for (;;) {
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
        }).start();

    }

}
