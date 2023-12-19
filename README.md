# IoT Network Application
## Introduction
This IoT Network Application is designed to facilitate communication between IoT devices and a central server using UDP protocol. It enables the management of device statuses and log retrieval through a client-server architecture. This README outlines the application's protocol, setup, and usage instructions.

## Application Protocol
### Protocol Overview
- Protocol Type: UDP (User Datagram Protocol)
- Ports:
  - Device Port: 1234 (Default)
  - Client Port: 5678 (Default)

### Initiating Connection
- Server: Listens on two separate ports for devices and clients.
- Client: Initiates communication with the server to request device information or logs.
- Device: Sends status updates to the server and responds to server requests.

### Messages/Actions

1. Request Device List
- Input: `LIST`
- Output: Comma-separated list of device IDs.
2. Request Device Status
- input: `<Device ID>`
- Output: Current status of the specified device.
3. Request Log (LOG)
- Input: `LOG <DeviceID>`
- Output: Log entries for the specified device.

## Building and Publishing with Docker
### Prerequisites
- Docker
- Java 17
- Maven
### Building the Application
1. Compile the application using Maven:
`mvn clean package`

2. Build Docker image:
`docker build -t iot-network-app .`

## Running the Application
### With Docker Compose
1. Start the application: `docker-compose up`
2. To interact with the client in a separate terminal: `docker attach <client-container-name>`

## Docker hub
https://hub.docker.com/repository/docker/ahmadjano/iot-network-app/general