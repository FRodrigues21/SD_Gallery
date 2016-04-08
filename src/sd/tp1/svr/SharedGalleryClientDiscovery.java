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

    private String address_s;
    private int server_port = 9000;
    private MulticastSocket server_socket = null;
    private InetAddress server_address = null;

    public SharedGalleryClientDiscovery(String address_s) {
        this.address_s = address_s;
    }

    @Override
    public void run() {

        try {
            server_address = InetAddress.getByName("224.1.2.8");
            if (!server_address.isMulticastAddress())
                System.exit(1);
            server_socket = new MulticastSocket(server_port);
            server_socket.joinGroup(server_address);
        }
        catch (IOException e) {
            System.out.println("SERVER ERROR: Could not create a client discovery!");
            System.exit(1);
        }

        while(true) {
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
    }

    private void sendIPToClient(String reply, InetAddress client_address, int client_port) {
        DatagramPacket p = new DatagramPacket(reply.getBytes(), reply.getBytes().length, client_address, client_port);
        try {
            server_socket.send(p);
        } catch (IOException e) {
            System.out.println("SERVER ERROR: Could not send packet with IP!");
        }
    }

}
