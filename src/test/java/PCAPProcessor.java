import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.protocol.voip.Rtp;
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

    public static void processPCAP(ResourceData resourceData, Protocol protocol) {
        List<Rtp> packetList = resourceData.getPacketList();
        packetList.forEach(packet -> {
            byte[] buffer = packet.getPayload();
            DatagramSocket socket = null;
            int port = 0;
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            dp.setAddress(protocol.getConsumerIp());
//            if (PCAPParser.parsePacket(packet.toString()).contains("RTP")) {
                if (PCAPParser.isAudioRTP(packet.toString())) {
                    port = protocol.getRtpAudioPortConsumer();
                    socket = protocol.getRtpAudioSocket();

                } else {
                    port = protocol.getRtpVideoPortConsumer();
                    socket = protocol.getRtpVideoSocket();
                }
//            } else if (PCAPParser.parsePacket(packet.toString()).contains("RTCP")) {
//
//                if (packet.toString().contains(protocol.getRtcpVideoSSRC())) {
//                    port = protocol.getRtcpVideoPortConsumer();
//                    socket = protocol.getRtcpVideoSocket();
//                } else {
//                    port = protocol.getRtcpAudioPortConsumer();
//                    socket = protocol.getRtcpAudioSocket();
//                }
//            }
//            try {
//                if (socket == null) {
//                    throw new Exception("Unknown packet type");
//                }
            dp.setPort(port);
            try {
                socket.send(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
