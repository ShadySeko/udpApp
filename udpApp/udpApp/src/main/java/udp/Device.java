// Device.java
package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Device {
    private String status = "default";
    private DatagramSocket socket;
    private InetAddress serverAddress = InetAddress.getLocalHost(); // default
    private int serverPort;
    private String type = "device";
    private final long id;

    private final ArrayList<String> statusHistory = new ArrayList<>(); // Store the log


    public Device(String type, String status, String serverIp, int serverPort) throws Exception {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.type = type;
        this.status = status;
        this.id = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(4));
    }

    public String getStatus() {
        return status;
    }

    public void sendStatus() throws Exception {
        byte[] buffer = status.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        System.out.println("Sending status: to " + this.serverAddress + ":" + this.serverPort);
        socket.send(packet);
    }

    public void changeStatus(String newStatus) throws Exception {
        this.status = newStatus;
        statusHistory.add(newStatus);
        String message = id + " " + type + " " + status + " at_" + LocalDateTime.now();
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        System.out.println("Sending status: to " + serverAddress + ":" + serverPort);
        socket.send(packet);
    }

    public ArrayList<String> getStatusHistory() {
        return statusHistory;
    }

    public void changeStatusNoNotify(String newStatus) throws Exception {
        this.status = newStatus;
        this.statusHistory.add(newStatus + " at " + LocalDateTime.now());
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
        Thread listenThread = new Thread(this::listenForRequests);
        Thread replThread = new Thread(this::startRepl);
        listenThread.start();
        replThread.start();

        try {
            listenThread.join();
            replThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}