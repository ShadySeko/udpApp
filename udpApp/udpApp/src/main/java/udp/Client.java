package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class Client {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private static final int TIMEOUT = 5000; // Timeout in milliseconds
    private static final int MAX_RETRIES = 3; // Maximum number of retries

    public Client(String serverIp, int serverPort) throws Exception {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.socket.setSoTimeout(TIMEOUT);
    }

    public void requestStatus(String deviceIp) throws Exception {
        byte[] buffer = deviceIp.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
    }

    public String requestDeviceCount() throws Exception {
        byte[] buffer = "device_count".getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public void startRepl() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter command (device IP to request status or 'device_count' to get list of devices): ");
            String command = scanner.nextLine();
            try {
                if ("device_count".equals(command)) {
                    int retries = 0;
                    while (retries < MAX_RETRIES) {
                        try {
                            String deviceList = requestDeviceCount();
                            System.out.println("Available devices: " + deviceList);
                            break;
                        } catch (SocketTimeoutException e) {
                            retries++;
                            if (retries == MAX_RETRIES) {
                                System.out.println("Request timed out after " + MAX_RETRIES + " attempts.");
                            }
                        }
                    }
                } else {
                    int retries = 0;
                    while (retries < MAX_RETRIES) {
                        try {
                            requestStatus(command);
                            break;
                        } catch (SocketTimeoutException e) {
                            retries++;
                            if (retries == MAX_RETRIES) {
                                System.out.println("Request timed out after " + MAX_RETRIES + " attempts.");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}