package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class Device {
    private static final Logger LOGGER = Logger.getLogger(Device.class.getName());
    private String status;
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final int serverPort;
    private final String type;
    private final long id;
    private final List<String> statusHistory;

    public Device(String type, String initialStatus, String serverIp, int serverPort) throws Exception {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.type = type;
        this.id = generateUniqueId();
        this.statusHistory = new ArrayList<>();
        changeStatusNoNotify(initialStatus);
    }

    private long generateUniqueId() {
        return Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(4));
    }

    public void start() {
        LOGGER.info("Device " + type + " started at " + LocalDateTime.now());
        Thread listenForRequests = new Thread(this::listenForRequests);
        Thread startRepl = new Thread(this::startRepl);
        listenForRequests.start();
        startRepl.start();


        try {
            listenForRequests.join();
            startRepl.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startRepl() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter new status: ");
            String newStatus = scanner.nextLine();
            if (!newStatus.trim().isEmpty()) {
                notifyNewStatus(newStatus);
            } else {
                LOGGER.warning("Empty status is not allowed.");
            }
        }
    }

    private void listenForRequests() {
        while (true) {
            try {
                DatagramPacket packet = receivePacket();
                sendStatusUpdate(packet);
            } catch (Exception e) {
                LOGGER.severe("Error in listenForRequests: " + e.getMessage());
            }
        }
    }

    private DatagramPacket receivePacket() throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }

    private void sendStatusUpdate(DatagramPacket requestPacket) throws Exception {
        byte[] buffer = status.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, requestPacket.getAddress(), requestPacket.getPort());
        socket.send(responsePacket);
    }

    public void notifyNewStatus(String newStatus) {
        if (!newStatus.trim().isEmpty() && !newStatus.equals(this.status)) {
            String statusEntry = formatStatusEntry(newStatus);
            System.out.println("Status changed to " + newStatus);
            try {
                sendStatusUpdateToServer(statusEntry);
            } catch (Exception e) {
                LOGGER.severe("Failed to send status update: " + e.getMessage());
            }
        } else if (newStatus.trim().isEmpty()) {
            LOGGER.warning("Empty status is not allowed.");
        }
    }

    public void changeStatusNoNotify(String newStatus) {
        this.status = newStatus;
        this.statusHistory.add(newStatus + " at " + LocalDateTime.now());
    }


    private String formatStatusEntry(String status) {
        return id + "," + type + "," + status + ",at_" + LocalDateTime.now();
    }

    private void sendStatusUpdateToServer(String statusEntry) throws Exception {
        byte[] buffer = statusEntry.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public List<String> getStatusHistory() {
        return new ArrayList<>(statusHistory);
    }
}
