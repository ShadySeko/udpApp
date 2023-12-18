// Commands.java
package udp;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "app", mixinStandardHelpOptions = true, version = "udp 1.0",
        description = "UDP client/server application",
        subcommands = {Commands.ServerCommand.class, Commands.ClientCommand.class, Commands.DeviceCommand.class})
public class Commands implements Runnable {
    @Override
    public void run() {
        // default behavior when no subcommand is specified
    }
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Commands()).execute(args);
        System.exit(exitCode);
    }

    @Command(name = "server", description = "Start server")
    public static class ServerCommand implements Runnable {


        @Option(names = {"-d", "--device-port"}, description = "Device port")
        private int devicePort = 1234;

        @Option(names = {"-c", "--client-port"}, description = "Client port")
        private int clientPort = 5678;



        @Override
        public void run() {
            try {
                Server server = new Server(devicePort, clientPort);
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Command(name = "client", description = "Start client")
    public static class ClientCommand implements Runnable {

        @Option(names = {"-p", "--port"}, description = "Port")
        private int port = 5678;

        @Override
        public void run() {
            try {
                Client client = new Client("localhost", port);
                client.startRepl();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Command(name = "device", description = "Start device")
    public static class DeviceCommand implements Runnable {

        @Option(names = {"-p", "--port"}, description = "Port")
        private int port = 1234;

        @Option(names = {"-t", "--type"}, description = "Type")
        private String type = "default";

        @Override
        public void run() {
            try {
                Device device = new Device(type,"default", "localhost", port);
                device.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
