import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import protocol.Protocol;
import resource.ResourceData;
import resource.ResourceMapping;
import resource.exceptions.NotFoundResourceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static int count = 0;

    public static void main(String[] args) throws IOException, NotFoundResourceException, InterruptedException {
        ResourceData re = new ResourceData();
        re.setFilename("resources/bunny.pcapng");
        re.setDuration(598.48);
        StringBuilder errbuf = new StringBuilder();
        Pcap pcap = Pcap.openOffline("resources/bunny.pcapng", errbuf);
        List<String> packetList = new ArrayList<>();
        int dataCountPacket = 15;
        pcap.loop(-1, new JPacketHandler() {
            @Override
            public void nextPacket(JPacket jPacket, Object o) {
                if (count > dataCountPacket) {
                    packetList.add(jPacket.toString());
                }
                count++;
            }
        }, errbuf);
        re.setPacketList(packetList);
//
        ResourceMapping.addResource("ad2e", re);
        Server server = new Server();
        server.start(5554);
//
    }

}