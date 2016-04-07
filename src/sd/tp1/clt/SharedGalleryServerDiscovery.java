package sd.tp1.clt;

import sd.tp1.gui.Gui;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedGalleryServerDiscovery implements Runnable{

    private Gui gui;
    private int client_port = 9000;
    private InetAddress client_address = null;
    private MulticastSocket client_socket = null;

    private Map<String,Request> servers = new ConcurrentHashMap<>();

    public Map<String,Request> getServers() {
        return servers;
    }

    public SharedGalleryServerDiscovery(Gui gui) {
        this.gui = gui;
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
            e.printStackTrace();
        }

        // Receives packets with SERVER IP
        Thread r = new Thread(new Runnable() {
            @Override
            public void run()
            {
                System.err.println("RECEIVING THREAD STARTED!");
                while(true) {
                    DatagramPacket reply;
                    byte [] buffer = new byte[65536];
                    reply = new DatagramPacket(buffer, buffer.length);
                    try {
                        client_socket.setSoTimeout(1000);
                        client_socket.receive(reply);
                        if(reply.getLength() > 0) {
                            String url = new String(reply.getData(), 0, reply.getLength());
                            System.err.println("FOUND: " + url);
                            if(!servers.containsKey(url) && url.contains("http")) {
                                if (url.contains("SOAP")) {
                                    System.err.println("ADDED: " + url);
                                    servers.put(url, new RequestSOAP(url));
                                    gui.updateAlbums();
                                }
                                else if(url.contains("REST")) {
                                    System.err.println("ADDED: " + url);
                                    servers.put(url, new RequestREST(url));
                                    gui.updateAlbums();
                                }
                            }
                        }
                    } catch (IOException e) {
                        // TODO
                    }
                }

            }
        });
        r.start();

        // Sends packets to Server
        Thread s = new Thread(new Runnable() {
            @Override
            public void run() {
                System.err.println("SENDING THREAD STARTED!");
                while (true) {
                    System.err.println("SENDING SERVER DISCOVERY!");
                    try {
                        DatagramPacket request;
                        String data_req = "FileServer";
                        byte [] data_cont = data_req.getBytes();
                        request = new DatagramPacket(data_cont, data_cont.length, client_address, client_port);
                        client_socket.send(request);
                        Thread.sleep(5000);
                    } catch (IOException | InterruptedException e) {
                        System.err.println("ERRO NO DISCOVERY 2");
                    }
                }
            }
        });
        s.start();

    }

}
