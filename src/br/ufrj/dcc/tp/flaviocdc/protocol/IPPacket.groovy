package br.ufrj.dcc.tp.flaviocdc.protocol

import java.nio.ByteBuffer

class IPPacket {

    Inet4Address sourceIP;
    Inet4Address destinationIP;
    byte[] data;
    byte protocol = Protocol.TRANSPORT_TCP;

    byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(sourceIP.getAddress());
        bos.write(destinationIP.getAddress());
        bos.write((short) protocol);
        bos.write(data);

        return bos.toByteArray();
    }

    static IPPacket fromByteArray(byte[] array) {
        return new IPPacket(
                sourceIP: InetAddress.getByAddress(array[0..3] as byte[]),
                destinationIP: InetAddress.getByAddress(array[4..7] as byte[]),
                protocol: array[8],
                data: array[9..(array.length-1)] as byte[]
        );
    }

    public String toString() {
        return "IPPacket[sourceIP=${sourceIP.getHostAddress()}, destinationIP=${destinationIP.getHostAddress()}, data=${data}]";
    }
}
