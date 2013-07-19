package br.ufrj.dcc.tp.flaviocdc

import br.ufrj.dcc.tp.flaviocdc.protocol.Frame
import br.ufrj.dcc.tp.flaviocdc.protocol.MAC

class Switch {

    private ServerSocket serverSocket;
    private List<Socket> ports = [];
    private Map<MAC, Socket> forwardingTable = [:];

    void on() {
        serverSocket = new ServerSocket(6000);
        Thread.start("SwitchThread", this.&handleConnection);
    }

    void handleConnection() {
        while (true) {
            switchPrint "Waiting for host to connect...";

            Socket socket = serverSocket.accept();
            int port = ports.size();

            switchPrint "Client connected to switch port #${ports.size()}";

            ports << socket;

            new Thread(new HostHandler(hostSocket: socket), "Switch-Host-Port${port}").start();
        }
    }

    class HostHandler implements Runnable {
        Socket hostSocket;

        @Override
        void run() {
            switchPrint "Starting host handler...";

            while (true) {
                byte[] buf = new byte[1500];
                hostSocket.inputStream.read(buf);

                Frame frame = Frame.fromByteArray(buf);
                switchPrint "frame received ${frame}";

                handleForwardingTable(frame);
                forwardFrame(frame)
            }
        }

        private void forwardFrame(Frame frame) {
            Socket destination = forwardingTable[frame.destinationMAC];
            if (destination == null) {
                switchPrint "${frame.destinationMAC} is not on the forwarding table, broadcasting...";
                //frame.destinationMAC = Frame.BROADCAST;
                ports.each {
                    if (it.equals(hostSocket)) {
                        // do not return the frame to the source port
                        return;
                    }

                    it.outputStream.write(frame.toByteArray());
                    it.outputStream.flush();
                }
            } else {
                destination.outputStream.write(frame.toByteArray());
                destination.outputStream.flush();
            }
        }

        void handleForwardingTable(Frame frame) {
            Socket source = forwardingTable[frame.sourceMAC];
            if (source == null) {
                switchPrint "adding ${frame.sourceMAC} to the forwarding table";
                forwardingTable.put(frame.sourceMAC, hostSocket);
            }
        }
    }

    def switchPrint(String str) {
        println "# SWITCH: ${str}";
    }
}
