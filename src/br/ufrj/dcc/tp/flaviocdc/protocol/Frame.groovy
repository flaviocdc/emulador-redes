package br.ufrj.dcc.tp.flaviocdc.protocol

import java.nio.ByteBuffer

class Frame {

    static final MAC BROADCAST = new MAC("FF:FF:FF:FF:FF:FF");

    MAC sourceMAC;
    MAC destinationMAC;
    byte protocol;
    byte[] data;

    byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(sourceMAC.octets);
        bos.write(destinationMAC.octets);
        bos.write(protocol);
        bos.write(data);

        return bos.toByteArray();
    }

    static Frame fromByteArray(byte[] array) {
        Frame frame = new Frame(
            sourceMAC: new MAC(array[0..5] as byte[]),
            destinationMAC: new MAC(array[6..11] as byte[]),
            protocol: array[12],
            data: array[13..(array.length-1)] as byte[]
        );

        return frame;
    }

    public String toString() {
        return "Frame[sourceMAC=${sourceMAC}, destMAC=${destinationMAC}, protocol=${protocol}, data=${data}]";
    }
}
