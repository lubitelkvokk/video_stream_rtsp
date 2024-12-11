import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import protocol.Protocol;
import resource.ResourceData;
import resource.ResourceMapping;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class PCAPProcessor {

    public static void processPCAP(ResourceData resourceData, Protocol protocol) throws InterruptedException {
        List<String> packetList = resourceData.getPacketList();
        TimeUnit.SECONDS.sleep(3);
        packetList.forEach(packet -> {
            byte[] buffer = new byte[4096];
            DatagramSocket socket = null;
            int port = 0;
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            dp.setAddress(protocol.getConsumerIp());
            if (PCAPParser.parsePacket(packet.toString()).contains("RTP")) {
                if (PCAPParser.isAudioRTP(packet.toString())) {
                    port = protocol.getRtpAudioPortConsumer();
                    socket = protocol.getRtpAudioSocket();
                } else {
                    port = protocol.getRtpVideoPortConsumer();
                    socket = protocol.getRtpVideoSocket();
                }
            } else if (PCAPParser.parsePacket(packet.toString()).contains("RTCP")) {

                if (packet.toString().contains(protocol.getRtcpVideoSSRC())) {
                    port = protocol.getRtcpVideoPortConsumer();
                    socket = protocol.getRtcpVideoSocket();
                } else {
                    port = protocol.getRtcpAudioPortConsumer();
                    socket = protocol.getRtcpAudioSocket();
                }
            }
            dp.setPort(port);
            try {
                if (socket == null) {
                    throw new Exception("Unknown packet type");
                }
                socket.send(dp);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
