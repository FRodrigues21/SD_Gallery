package sd.tp1.svr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

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
            server_address = InetAddress.getByName("224.1.2.3");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (!server_address.isMulticastAddress()) {
            System.out.println("The address is not multicast!");
            System.exit(1);
        }
        try {
            server_socket = new MulticastSocket(server_port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Join a multicast group
        try {
            server_socket.joinGroup(server_address);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private void sendIPToClient(String reply, InetAddress client_address, int client_port) {
        DatagramPacket p = new DatagramPacket(reply.getBytes(), reply.getBytes().length, client_address, client_port);
        try {
            server_socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
