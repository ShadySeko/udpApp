// Device.java
package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Device {
    private String status = "default";
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    private String type = "device";

    public Device(String type, String status, String serverIp, int serverPort) throws Exception {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.type = type;
    }



    public void sendStatus() throws Exception {
        byte[] buffer = status.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
    }

    public void changeStatus(String newStatus) throws Exception {
        this.status = newStatus;
        String message = type + " " + status + " at " + LocalDateTime.now();
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
    }

    public void listenForRequests() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                buffer = status.getBytes();
                packet = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRepl() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter new status: ");
            String newStatus = scanner.nextLine();
            try {
                changeStatus(newStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        System.out.println("Device" + " " + type + " started at " + LocalDateTime.now());
        new Thread(this::listenForRequests).start();
        new Thread(this::startRepl).start();
    }
}