package sd.tp1.svr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.crypto.Data;
import javax.xml.ws.Endpoint;

@WebService
public class FileServerSOAP {

    private File basePath;

    public FileServerSOAP() {
        this(".");
    }

    public FileServerSOAP(String path) {
        super();
        basePath = new File(path);
    }

    @WebMethod
    public String test() {
        return null;
    }

    public static void main(String args[]) throws Exception {
        String path = args.length > 0 ? args[0] : ".";
        Endpoint.publish("http://0.0.0.0:8080/FileServer", new FileServerSOAP(path));
        System.err.println("FileServerSOAP: Started");

        //Create a multicast socket
        final int port = 9000;
        final InetAddress address = InetAddress.getByName("224.1.2.3");
        if (!address.isMulticastAddress()) {
            System.out.println("The address is not multicast!");
            System.exit(1);
        }
        MulticastSocket socket = new MulticastSocket(port);

        // Join a multicast group
        socket.joinGroup(address);

        // Wait for request and reply
        DatagramPacket incoming;
        DatagramPacket reply;
        while (true) {
            // Request received
            byte [] buffer = new byte[65536];
            incoming = new DatagramPacket(buffer, buffer.length);
            socket.receive(incoming);
            if(incoming.getLength() > 0) {
                String str_reply = new String(incoming.getData(), 0, incoming.getLength());
                System.out.println("Request received: " + str_reply);
                if (str_reply.contains("FileServer")) {
                    // Reply with address
                    String fileserver = "http://0.0.0.0:8080/FileServer";
                    reply = new DatagramPacket(fileserver.getBytes(), fileserver.getBytes().length);
                    reply.setPort(incoming.getPort());
                    reply.setAddress(incoming.getAddress());
                    System.out.println("Sending reply: " + new String(reply.getData(), 0, reply.getLength()));
                    socket.send(reply);
                }
            }
        }
    }

}
