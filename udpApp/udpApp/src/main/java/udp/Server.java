// Server.java
package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;
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

    public void start() throws UnknownHostException {
        System.out.println("Server started at " + InetAddress.getLocalHost() + " on ports " + deviceSocket.getLocalPort() + " and " + clientSocket.getLocalPort() + ".");

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
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Listening for device status on port " + deviceSocket.getLocalPort());
                deviceSocket.receive(packet);
                System.out.println("Received device status from " + packet.getAddress() + ":" + packet.getPort());
                String message = new String(packet.getData(), 0, packet.getLength());
                String[] parts = message.split(",");
                String id = parts[0];
                String status = parts[2];
                if(devices.containsKey(id)){
                    devices.get(id).changeStatusNoNotify(parts[2]);
                }else{
                devices.put(id, new Device(parts[1], parts[2], "localhost", 1234));
            }}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForClientRequests() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Listening for client requests... on port " + clientSocket.getLocalPort() + "...");
                clientSocket.receive(packet);
                System.out.println("Received client request from " + packet.getAddress() + ":" + packet.getPort());
                String request = new String(packet.getData(), 0, packet.getLength());
                String[] parts = request.split(" ");
                if ("LIST".equals(parts[0])) {
                    StringJoiner deviceList = new StringJoiner(",");
                    for (String deviceUuid : devices.keySet()) {
                        deviceList.add(devices.get(deviceUuid).getType() +" "+ deviceUuid);
                    }
                    // System.out.println(deviceList.toString());
                    buffer = deviceList.toString().getBytes();
                    packet = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                    clientSocket.send(packet);
                }else if("LOG".equals(parts[0])){
                    Device device = devices.get(parts[1]);
                    if (device != null) {
                        ArrayList<String> statusHistory = device.getStatusHistory();
                        StringJoiner statusList = new StringJoiner(",");
                        for (String status : statusHistory) {
                            statusList.add(status);
                        }
                        buffer = statusList.toString().getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                        clientSocket.send(packet);
                    }
                } else {
                    Device device = devices.get(request);
                    if (device != null) {
                        String status = device.getStatus();
                        buffer = status.getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                        clientSocket.send(packet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}