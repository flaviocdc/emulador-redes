package br.ufrj.dcc.tp.flaviocdc.protocol

class MAC {

    byte[] octets;

    public MAC(byte[] octets) {
        if (octets.length != 6)
            throw new RuntimeException("Invalid MAC octets ${octets}");

        this.octets = octets;
    }

    public MAC(String macString) {
        String[] octetsStrings = macString.split(":");
        if (octetsStrings.size() != 6)
            throw new RuntimeException("Invalid MAC Address ${macString}");

        octets = new byte[6];

        octetsStrings.eachWithIndex { String octetStr, int i ->
            byte octet = Integer.parseInt(octetStr, 16).byteValue()
            octets[i] = octet;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        octets.each {
            sb.append(String.format("%02X", it)).append(":");
        }
        sb.deleteCharAt(sb.size() - 1);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof MAC)) return false;
        MAC other = (MAC) o;

        return other.toString().equals(this.toString());
    }
}
