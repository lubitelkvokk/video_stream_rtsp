import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;

import java.util.ArrayList;
import java.util.List;

import static org.jnetpcap.protocol.voip.Sdp.Fields.Time;

public class PCAPProcessor {
    private static int count = 0;
    public static List<String> processPCAP(String filename) {
        List<String> list = new ArrayList<String>();
        StringBuilder errbuf = new StringBuilder();

        Pcap pcap = Pcap.openOffline(filename, errbuf);
        pcap.loop(-1, new JPacketHandler() {
            @Override
            public void nextPacket(JPacket packet,
                                   Object errbuf) {
                if (count < 70) {

//                    System.out.println(packet);
                }
                count++;
                list.add(
                       count + ":" + PCAPParser.parsePacket(packet.toString())
                );
                if (count < 30 && PCAPParser.parsePacket(packet.toString()).equals("RTP")) {
                    System.out.println(packet);
                }
            }
        }, errbuf);
        return list;

    }
}
