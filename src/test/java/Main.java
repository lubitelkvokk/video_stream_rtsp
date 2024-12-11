import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import resource.ResourceData;
import resource.ResourceMapping;
import resource.exceptions.NotFoundResourceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

private static int count = 0;

public static void main(String[] args) throws IOException, NotFoundResourceException, InterruptedException {

    // creating global resource data
    // ideally, a resource parsing module should be implemented
    ResourceData re = new ResourceData();
    re.setFilename("resources/bunny.pcapng");
    re.setDescribeArea("m=audio 0 RTP/AVP 96\r\n" +
            "a=rtpmap:96 mpeg4-generic/12000/2\r\n" +
            "a=fmtp:96 profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1490\r\n" +
            "a=control:trackID=1\r\n" +
            "m=video 0 RTP/AVP 97\r\n" +
            "a=rtpmap:97 H264/90000\r\n" +
            "a=fmtp:97 packetization-mode=1;profile-level-id=42C01E;sprop-parameter-sets=Z0LAHtkDxWhAAAADAEAAAAwDxYuS,aMuMsg==\r\n" +
            "a=cliprect:0,0,160,240\r\n" +
            "a=framesize:97 240-160\r\n" +
            "a=framerate:24.0\r\n" +
            "a=control:trackID=2\r\n");
    re.setDuration(598.48);
    StringBuilder errbuf = new StringBuilder();
    Pcap pcap = Pcap.openOffline("resources/bunny.pcapng", errbuf);
    List<JPacket> packetList = new ArrayList<>();

    Thread pcapThread = new Thread(() -> {
        pcap.loop(-1, new JPacketHandler() {
            @Override
            public void nextPacket(JPacket jPacket, Object o) {
                packetList.add(jPacket);
                count++;
            }
        }, errbuf);
    });

    pcapThread.start();

    pcapThread.join(); // Блокирует основной поток, пока pcapThread не завершится

    re.setPacketList(packetList);
    System.out.println(count);

    ResourceMapping.addResource("ad2e", re);
    Server server = new Server();
    server.start(5554);
}