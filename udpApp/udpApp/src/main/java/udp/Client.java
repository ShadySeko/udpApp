package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final int serverPort;
    private static final int TIMEOUT = 5000;
    private static final int MAX_RETRIES = 2;
    private static final int BUFFER_SIZE = 2048;
    private static final String COMMAND_LIST = "LIST";
    private static final String COMMAND_LOG = "LOG";

    public Client(String serverIp, int serverPort) throws Exception {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.socket.setSoTimeout(TIMEOUT);
    }

    private String sendRequest(String request) throws Exception {
        byte[] buffer = request.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
        socket.send(packet);
        buffer = new byte[BUFFER_SIZE];
        packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public String requestLog(String deviceIp) throws Exception {
        return sendRequest(COMMAND_LOG + " " + deviceIp);
    }

    public String requestStatus(String deviceIp) throws Exception {
        return sendRequest(deviceIp);
    }

    public String requestDeviceCount() throws Exception {
        return sendRequest(COMMAND_LIST);
    }

    private void processCommand(String command) {
        try {
            String response;
            if (COMMAND_LIST.equals(command)) {
                response = requestDeviceCount();
                System.out.println("Available devices: " + response);
            } else if (command.startsWith(COMMAND_LOG)) {
                response = requestLog(command.split(" ")[1]);
                System.out.println("Status changes for " + command.split(" ")[1] + ":");
                for (String status : response.split(",")) {
                    System.out.println(status);
                }
            } else {
                response = requestStatus(command);
                System.out.println("Status of " + command + ": " + response);
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void startRepl() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter command ('LIST' for devices, '<ID>' for status, 'LOG <ID>' for log):");
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            processCommand(command);
        }
    }
}
