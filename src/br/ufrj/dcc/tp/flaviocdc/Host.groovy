package br.ufrj.dcc.tp.flaviocdc

import br.ufrj.dcc.tp.flaviocdc.protocol.ARP
import br.ufrj.dcc.tp.flaviocdc.protocol.Frame
import br.ufrj.dcc.tp.flaviocdc.protocol.IPPacket

import br.ufrj.dcc.tp.flaviocdc.protocol.MAC
import br.ufrj.dcc.tp.flaviocdc.protocol.Protocol

class Host {

    private Inet4Address borderIP;
    private Socket switchSocket;

    Inet4Address localIP;
    MAC mac;

    private Map<Byte, LinkedList<IPPacket>> buffers = [:];
    private Map<Inet4Address, MAC> arpCache = [:];

    int N_Activate_Request(Inet4Address borderIP, Inet4Address localIP, Inet4Address switchIP, String mac) {
        this.borderIP = borderIP;
        this.localIP = localIP;
        this.mac = new MAC(mac);

        try {
            switchSocket = new Socket(switchIP, 6000);

            Thread.start(this.&worker)
        } catch (Exception e) {
            e.printStackTrace()
            return 0;
        }

        return 1;
    }

    int N_Data_Request(byte protocol, Inet4Address destinationIP, byte[] data) {
        IPPacket pkt = new IPPacket(
          sourceIP: localIP,
          destinationIP: destinationIP,
          data: data,
          protocol: protocol
        );

        Frame frame = new Frame(
            sourceMAC: mac,
            destinationMAC: lookupARP(destinationIP),
            protocol: Protocol.NETWORK_IP,
            data: pkt.toByteArray()
        );

        assert frame.data.length <= 1500;

        try {
            sendToPhysicalLayer(frame);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        return 1;
    }

    int N_Data_Indication(byte protocol) {
       List<IPPacket> buffer = buffers.get(protocol, []);

       return (buffer.size() == 0 ? 0 : 1);
    }

    // FIXME mudei a assinatura do metodo
    IPPacket N_Data_Receive(byte protocol) {
        LinkedList<IPPacket> buffer = buffers.get(protocol);
        return buffer.pollFirst();
    }

    int N_Deactivate_Request() {
        try {
            switchSocket.disconnect();
            switchSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        return 1;
    }

    void worker() {
        hostPrint "Starting thread for Host with ${mac} MAC"

        while (true) {
            byte[] data = new byte[1500];
            switchSocket.inputStream.read(data);

            Frame frame = Frame.fromByteArray(data);
            hostPrint "Frame received: ${frame}";

            if (!frame.destinationMAC.equals(mac) && !frame.destinationMAC.equals(Frame.BROADCAST)) {
                hostPrint "Alien MAC address arrived! Ignoring the whole frame";
                continue;
            }

            byte linkProtocol = frame.protocol;
            if (linkProtocol == Protocol.NETWORK_IP) {
                handleIP(frame)
            } else if (linkProtocol == Protocol.NETWORK_ARP) {
                handleARP(frame)
            } else {
                hostPrint "Frame contained unsupported protcol: ${linkProtocol}";
            }
        }
    }

    private void handleIP(Frame frame) {
        IPPacket pkt = IPPacket.fromByteArray(frame.data);
        byte networkProtocol = pkt.protocol;

        if (!buffers.containsKey(networkProtocol)) {
            buffers.put(networkProtocol, new LinkedList<IPPacket>());
        }

        buffers.get(networkProtocol,).add(pkt);
        hostPrint "IP packet received: ${pkt}";
    }

    private void handleARP(Frame frame) {
        ARP arp = ARP.fromByteArray(frame.data);

        // algum host fazendo broadcast arp pare descobrir meu IP
        if (arp.requestedMAC.equals(Frame.BROADCAST) && arp.requestedIP.equals(localIP)) {
            // garantindo integridade dos pacotes
            assert arp.requestedMAC.equals(frame.destinationMAC);

            hostPrint "ARP: there's a host broadcasting to figure my mac address ${arp}";

            ARP responseARP = new ARP(
                    requesterMAC: arp.requesterMAC,
                    requestedMAC: mac,
                    requestedIP: arp.requestedIP
            );

            hostPrint "ARP: replying with ${responseARP}";

            Frame responseFrame = new Frame(
                    sourceMAC: mac,
                    destinationMAC: arp.requesterMAC,
                    protocol: Protocol.NETWORK_ARP,
                    data: responseARP.toByteArray()
            );

            sendToPhysicalLayer(responseFrame.toByteArray());
        }

        // algum ARP chegou como resposta a um broadcast que partiu desse host
        if (arp.requesterMAC.equals(mac)) {
            hostPrint "ARP: a response to an ARP request arrived ${arp}";
            MAC mac = arp.requestedMAC;
            Inet4Address ip = arp.requestedIP;

            arpCache[ip] = mac;
            hostPrint "ARP: added ${ip}->${mac} to the arp table";
        }
    }

    private MAC lookupARP(Inet4Address ip) {
        MAC mac = arpCache[ip];

        if (mac == null) {
            hostPrint "${ip.hostAddress} not found in arp cache, broadcasting arp"

            ARP arp = new ARP(
                    requesterMAC: this.mac,
                    requestedMAC: Frame.BROADCAST,
                    requestedIP: ip
            );

            Frame arpFrame = new Frame(
                    sourceMAC: this.mac,
                    destinationMAC: Frame.BROADCAST,
                    protocol: Protocol.NETWORK_ARP,
                    data: arp.toByteArray()
            );

            sendToPhysicalLayer(arpFrame.toByteArray());

            while (arpCache[ip] == null) {
                hostPrint "waiting for arp reply...";
                Thread.sleep(50);
            }

            mac = arpCache[ip];
            hostPrint "got arp reply, moving on... (${ip}->${mac})";
        }

        return mac;
    }

    def hostPrint(String str) {
        println "## HOST[${mac}]: ${str}";
    }

    def sendToPhysicalLayer(Frame frame) {
        switchSocket.outputStream.write(frame.toByteArray());
        switchSocket.outputStream.flush();
    }
}