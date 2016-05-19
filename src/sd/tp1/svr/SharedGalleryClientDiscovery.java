package sd.tp1.svr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
 */
public class SharedGalleryClientDiscovery implements Runnable {

    private String address_s; // Server address
    private String server_multicast = "224.1.2.8"; // Multicast IP
    private int server_port = 9000; // Multicast Port
    private InetAddress server_address = null; // Server address
    private MulticastSocket server_socket = null; // Multicast Socket


    public SharedGalleryClientDiscovery(String address_s) {
        this.address_s = address_s;
    }

    @Override
    public void run() {

        try {
            server_address = InetAddress.getByName(server_multicast);
            if (!server_address.isMulticastAddress())
                System.exit(1);
            server_socket = new MulticastSocket(server_port);
            server_socket.joinGroup(server_address);
        }
        catch (IOException e) {
            System.out.println("SERVER ERROR: Could not create a client discovery!");
            e.printStackTrace();
            System.exit(1);
        }

        // Waits for packets containing FileServer question and replies with IP address
        new Thread(() -> {
            for(;;) {
                try {
                    byte [] buffer = new byte[65536];
                    DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                    server_socket.receive(incoming);
                    if(incoming.getLength() > 0) {
                        String incoming_message = new String(incoming.getData(), 0, incoming.getLength());
                        if (incoming_message.contains("FileServer"))
                            sendIPToClient(address_s, incoming.getAddress(), incoming.getPort());
                    }
                    else
                        Thread.sleep(5000);
                }
                catch (IOException | InterruptedException e) {
                    System.out.println("SERVER ERROR: No packet received!");
                }
            }
        }).start();

    }

    // Send server IP Address to client
    private void sendIPToClient(String reply, InetAddress client_address, int client_port) {
        DatagramPacket p = new DatagramPacket(reply.getBytes(), reply.getBytes().length, client_address, client_port);
        try {
            server_socket.send(p);
        } catch (IOException e) {
            System.out.println("SERVER ERROR: Could not send packet with IP!");
        }
    }

}
