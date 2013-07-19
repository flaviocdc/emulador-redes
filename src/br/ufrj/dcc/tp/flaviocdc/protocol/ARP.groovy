package br.ufrj.dcc.tp.flaviocdc.protocol

class ARP {

    MAC requesterMAC;
    Inet4Address requestedIP;
    MAC requestedMAC;

    byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(requesterMAC.octets);
        bos.write(requestedMAC.octets);
        bos.write(requestedIP.address);

        return bos.toByteArray();
    }

    static ARP fromByteArray(byte[] array) {
        return new ARP(
                requesterMAC: new MAC(array[0..5] as byte[]),
                requestedMAC: new MAC(array[6..11] as byte[]),
                requestedIP: InetAddress.getByAddress(array[12..15] as byte[]),
        );
    }

    public String toString() {
        return "ARP[requesterMAC=${requesterMAC}, requestedMAC=${requestedMAC}, ip=${requestedIP}]";
    }
}