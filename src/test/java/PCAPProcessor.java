import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import protocol.Protocol;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class PCAPProcessor {
    private static final int count = 0;

    public static List<String> processPCAP(String filename, Protocol protocol) throws InterruptedException {
        List<String> list = new ArrayList<String>();
        StringBuilder errbuf = new StringBuilder();

        Pcap pcap = Pcap.openOffline(filename, errbuf);
        TimeUnit.SECONDS.sleep(3);
        pcap.loop(-1, new JPacketHandler() {
            @Override
            public void nextPacket(JPacket packet,
                                   Object errbuf) {
                int port;
                byte[] buffer = new byte[4096];
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                dp.setAddress(protocol.getConsumerIp());
                if (PCAPParser.parsePacket(packet.toString()).contains("RTP")) {
                    if (PCAPParser.isAudioRTP(packet.toString())) {
                        dp.setPort(protocol.getRtpVideoPort());
                        protocol.getSocket().bind()
                    } else {
                        protocol.putRTPVideoPackets(packet);
                    }
                } else if (PCAPParser.parsePacket(packet.toString()).contains("RTCP")) {

                    if (packet.toString().contains("destination = 49189")) {
                        protocol.putRTCPVideoPackets(packet);
                    } else {
                        protocol.putRTCPAudioPackets(packet);
                    }

                }
            }
        }, errbuf);
        return list;

    }
}
