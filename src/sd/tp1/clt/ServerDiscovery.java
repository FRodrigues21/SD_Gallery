package sd.tp1.clt;

import sd.tp1.clt.Client;
import sd.tp1.clt.ClientSOAP;
import sd.tp1.gui.Gui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by franciscorodrigues on 04/04/16.
 */
public class ServerDiscovery implements Runnable{

    private Gui gui;
    private Map<String,Client> servers = new ConcurrentHashMap<>();

    public Map<String,Client> getServers() {
        return servers;
    }

    public ServerDiscovery(Gui gui) {
        this.gui = gui;
    }

    public void run() {
        while(true) {
            System.err.println("ENTROU THREAD");
            int client_port = 9000;
            InetAddress client_address = null;
            try {
                client_address = InetAddress.getByName("224.1.2.3");
            } catch (UnknownHostException e) {
                System.err.println("ERRO NO DISCOVERY 1");
            }

            MulticastSocket socket = null;
            try {
                socket = new MulticastSocket();
                DatagramPacket request;
                String data_req = "FileServer";
                byte [] data_cont = data_req.getBytes();
                request = new DatagramPacket(data_cont, data_cont.length, client_address, client_port);
                socket.send(request);
            } catch (IOException e) {
                System.err.println("ERRO NO DISCOVERY 2");
            }

            DatagramPacket reply;

            byte [] buffer = new byte[65536];

            reply = new DatagramPacket(buffer, buffer.length);
            try {
                socket.setSoTimeout(2000);
                socket.receive(reply);
                if(reply.getLength() > 0) {
                    String url = new String(reply.getData(), 0, reply.getLength());
                    if(!servers.containsKey(url) && url.contains("http")) {
                        if (!url.contains("REST")) {
                            System.err.println("Found SOAP: " + url);
                            servers.put(url, new ClientSOAP(url));
                            gui.updateAlbums();
                        }
                    }
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("ERRO NO DISCOVERY 3");
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
