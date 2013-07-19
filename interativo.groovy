import br.ufrj.dcc.tp.flaviocdc.Host
import br.ufrj.dcc.tp.flaviocdc.Switch
import br.ufrj.dcc.tp.flaviocdc.protocol.Frame
import br.ufrj.dcc.tp.flaviocdc.protocol.IPPacket
import br.ufrj.dcc.tp.flaviocdc.protocol.MAC
import br.ufrj.dcc.tp.flaviocdc.protocol.Protocol

Inet4Address localhost = (Inet4Address) InetAddress.getByName("192.168.1.109");

Switch switchz = new Switch();
switchz.on();

Host host1 = new Host();
host1.N_Activate_Request(localhost, InetAddress.getByName("10.0.0.1"), localhost, "00:01:02:03:04:05");

Host host2 = new Host();
host2.N_Activate_Request(localhost, InetAddress.getByName("10.0.0.2"), localhost, "00:01:02:03:04:06");


host1.N_Data_Request(Protocol.TRANSPORT_TCP, host2.localIP, [0xCA, 0xFE, 0xBA, 0xBE] as byte[]);
host2.N_Data_Request(Protocol.TRANSPORT_TCP, host1.localIP, [0xCA, 0xFE, 0xBA, 0xBE] as byte[]);