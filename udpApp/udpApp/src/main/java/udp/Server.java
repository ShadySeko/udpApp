package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final int BUFFER_SIZE = 1024;

    private final DatagramSocket deviceSocket;
    private final DatagramSocket clientSocket;
    private final ConcurrentHashMap<String, Device> devices;

    public Server(int devicePort, int clientPort) throws Exception {
        this.deviceSocket = new DatagramSocket(devicePort);
        this.clientSocket = new DatagramSocket(clientPort);
        this.devices = new ConcurrentHashMap<>();
    }

    public void start() throws UnknownHostException {
        LOGGER.info("Server started at " + InetAddress.getLocalHost() + " on ports " + deviceSocket.getLocalPort() + " and " + clientSocket.getLocalPort() + ".");

        Thread deviceThread = new Thread(this::listenForDeviceStatus);
        Thread clientThread = new Thread(this::listenForClientRequests);
        deviceThread.start();
        clientThread.start();

        try {
            deviceThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void listenForDeviceStatus() {
        while (true) {
            try {
                DatagramPacket packet = receivePacket(deviceSocket);
                processDeviceStatus(packet);
            } catch (Exception e) {
                LOGGER.severe("Error in listenForDeviceStatus: " + e.getMessage());
            }
        }
    }

    private void listenForClientRequests() {
        while (true) {
            try {
                DatagramPacket packet = receivePacket(clientSocket);
                processClientRequest(packet);
            } catch (Exception e) {
                LOGGER.severe("Error in listenForClientRequests: " + e.getMessage());
            }
        }
    }

    private DatagramPacket receivePacket(DatagramSocket socket) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }

    private void processDeviceStatus(DatagramPacket packet) throws Exception {
        String message = new String(packet.getData(), 0, packet.getLength());
        String[] parts = message.split(",");
        String id = parts[0];
        if (devices.containsKey(id)) {
            devices.get(id).changeStatusNoNotify(parts[2]);
        } else {
            devices.put(id, new Device(parts[1], parts[2], "localhost", 1234));
        }
    }

    private void processClientRequest(DatagramPacket packet) throws Exception {
        String request = new String(packet.getData(), 0, packet.getLength());
        String[] parts = request.split(" ");
        if ("LIST".equals(parts[0])) {
            sendClientResponse(clientSocket, packet.getAddress(), packet.getPort(), getDeviceList());
        } else if ("LOG".equals(parts[0])) {
            sendClientResponse(clientSocket, packet.getAddress(), packet.getPort(), getDeviceLog(parts[1]));
        } else {
            sendClientResponse(clientSocket, packet.getAddress(), packet.getPort(), getDeviceStatus(request));
        }
    }

    private String getDeviceList() {
        StringJoiner deviceList = new StringJoiner(",");
        for (String deviceId : devices.keySet()) {
            deviceList.add(devices.get(deviceId).getType() + " " + deviceId);
        }
        return deviceList.toString();
    }

    private String getDeviceLog(String deviceId) {
        Device device = devices.get(deviceId);
        if (device != null) {
            StringJoiner statusList = new StringJoiner(",");
            for (String status : device.getStatusHistory()) {
                statusList.add(status);
            }
            return statusList.toString();
        }
        return "Device not found";
    }

    private String getDeviceStatus(String deviceId) {
        Device device = devices.get(deviceId);
        return device != null ? device.getStatus() : "Device not found";
    }

    private void sendClientResponse(DatagramSocket socket, InetAddress address, int port, String response) throws Exception {
        byte[] buffer = response.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }
}
