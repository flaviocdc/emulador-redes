import br.ufrj.dcc.tp.flaviocdc.protocol.*

MAC m = new MAC("0f:12:ad:35:fe:b3");
Inet4Address localhost = (Inet4Address) InetAddress.localHost;

println m.toString();

IPPacket pkt = new IPPacket(
        sourceIP: localhost,
        destinationIP: localhost,
        protocol: Protocol.TRANSPORT_TCP,
        data: [ 0xCA, 0xFE ] as byte[]
);

println pkt.toByteArray();
println IPPacket.fromByteArray(pkt.toByteArray()).toByteArray();
println pkt

Frame frame = new Frame(
        sourceMAC: new MAC("0f:12:ad:35:fe:b3"),
        destinationMAC: new MAC("0f:12:ad:35:fe:b4"),
        protocol: Protocol.NETWORK_IP,
        data: pkt.toByteArray()
)

byte[] frameBytes = frame.toByteArray()

println frameBytes;
println Frame.fromByteArray(frameBytes).toByteArray();
println frame.toString();