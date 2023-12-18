// Server.java
package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.StringJoiner;

public class Server {
    private DatagramSocket deviceSocket;
    private DatagramSocket clientSocket;
    private ConcurrentHashMap<String, Device> devices;

    public Server(int devicePort, int clientPort) throws Exception {
        this.deviceSocket = new DatagramSocket(devicePort);
        this.clientSocket = new DatagramSocket(clientPort);
        this.devices = new ConcurrentHashMap<>();
    }

    public void start() {
        new Thread(this::listenForDeviceStatus).start();
        new Thread(this::listenForClientRequests).start();
    }

    private void listenForDeviceStatus() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                deviceSocket.receive(packet);
                String status = new String(packet.getData(), 0, packet.getLength());
                devices.put(packet.getAddress().getHostAddress(), new Device("default", status, packet.getAddress().getHostAddress(), packet.getPort()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForClientRequests() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                clientSocket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength());
                if ("device_count".equals(request)) {
                    StringJoiner deviceList = new StringJoiner(",");
                    for (String deviceIp : devices.keySet()) {
                        deviceList.add(deviceIp);
                    }
                    buffer = deviceList.toString().getBytes();
                    packet = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                    clientSocket.send(packet);
                } else {
                    Device device = devices.get(request);
                    if (device != null) {
                        device.sendStatus();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}