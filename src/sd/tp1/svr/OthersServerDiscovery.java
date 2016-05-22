package sd.tp1.svr;

import sd.tp1.clt.Request;
import sd.tp1.clt.RequestREST;
import sd.tp1.clt.RequestSOAP;
import sd.tp1.clt.SharedGalleryContentProvider;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by franciscorodrigues on 22/05/16.
 */
public class OthersServerDiscovery implements Runnable {

    private String client_multicast = "224.1.2.8"; // Multicast IP
    private int client_port = 9000; // Multicast Port
    private InetAddress client_address = null; // Client address
    private MulticastSocket client_socket = null; // Socket Multicast
    private String url;
    private String local_password;
    private SharedGalleryContentProvider provider;

    private Map<String, Sync> servers = new ConcurrentHashMap<>(); // Map containing servers where the key is the server address and the value an object that handles the requests

    public OthersServerDiscovery(String url, String password) {
        this.url = url;
        local_password = password;
    }

    /**
     * Get the map of servers
     * @return the map of servers
     */
    public Map<String, Sync> getServers() {
        return servers;
    }

    /**
     * Removes server with a certain address
     * @param address - Address of a server
     */
    public void removeServer(String address) {
        servers.remove(address);
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
                    client_socket.setSoTimeout(2000);
                    client_socket.receive(reply);
                    if(reply.getLength() > 0) {
                        String url = new String(reply.getData(), 0, reply.getLength());
                        if(!servers.containsKey(url) && url.contains("http") && !url.contains(this.url)) {
                            if (url.contains("SOAP")) {
                                System.err.println("ADDED SERVER: " + url);
                                //servers.put(url, new SyncSOAP(url, local_password));
                            }
                            else if(url.contains("REST")) {
                                System.err.println("ADDED SERVER: " + url);
                                servers.put(url, new SyncREST(url, local_password));
                            }
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

    public Sync getServer() {
        for(Sync request : servers.values())
            return request;
        return null;
    }

}
