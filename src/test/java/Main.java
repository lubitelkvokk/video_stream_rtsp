import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.voip.Rtcp;
import org.jnetpcap.protocol.voip.Rtp;
import protocol.Protocol;
import resource.ResourceData;
import resource.ResourceMapping;
import resource.exceptions.NotFoundResourceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

private static int count = 0;

public static void main(String[] args) throws IOException, NotFoundResourceException, InterruptedException {
    ResourceData re = new ResourceData();
    re.setFilename("resources/bunny.pcapng");
    re.setDuration(598.48);
    StringBuilder errbuf = new StringBuilder();
    Pcap pcap = Pcap.openOffline("resources/bunny.pcapng", errbuf);
    List<JPacket> packetList = new ArrayList<>();
    pcap.loop(-1, new JPacketHandler() {
        @Override
        public void nextPacket(JPacket jPacket, Object o) {
            packetList.add(jPacket);
            count++;
        }
    }, errbuf);
    re.setPacketList(packetList);
    System.out.println(count);
//
    ResourceMapping.addResource("ad2e", re);
    Server server = new Server();
    server.start(5554);
//
}