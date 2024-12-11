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
        List<JPacket> packetList = resourceData.getPacketList();
        long[] videoPacketCount = {0}; // Счетчик отправленных RTP пакетов (видео)
        long[] videoOctetCount = {0};  // Счетчик отправленных байтов (видео)
        long[] audioPacketCount = {0}; // Счетчик отправленных RTP пакетов (аудио)
        long[] audioOctetCount = {0};  // Счетчик отправленных байтов (аудио)

//        new Thread(() -> {
//            try {
//                while (true) {
//                    TimeUnit.SECONDS.sleep(4); // Интервал отправки RTCP пакетов
//                    RTCPProcessor.sendSenderReport(
//                            protocol.getRtcpVideoSocket(),
//                            protocol.getConsumerIp(),
//                            protocol.getRtcpVideoPortConsumer(),
//                            (protocol.getRtcpVideoSSRC()),
//                            videoPacketCount[0],
//                            videoOctetCount[0]);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();

//        new Thread(() -> {
//            try {
//                while (true) {
//                    TimeUnit.SECONDS.sleep(4); // Интервал отправки RTCP пакетов
//                    RTCPProcessor.sendSenderReport(
//                            protocol.getRtcpAudioSocket(),
//                            protocol.getConsumerIp(),
//                            protocol.getRtcpAudioPortConsumer(),
//                            protocol.getRtcpAudioSSRC(),
//                            audioPacketCount[0],
//                            audioOctetCount[0]);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();

        // Обработка RTP пакетов
        packetList.forEach(packet -> {
            Rtp rtp = new Rtp();
            byte[] buffer;
            if (packet.hasHeader(rtp)) {
                buffer = Util.addArrays(rtp.getHeader(), rtp.getPayload());
                DatagramSocket socket = null;
                int port = 0;
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                dp.setAddress(protocol.getConsumerIp());

                if (PCAPParser.isAudioRTP(packet.toString())) {
                    port = protocol.getRtpAudioPortConsumer();
                    socket = protocol.getRtpAudioSocket();
                    audioPacketCount[0]++;
                    audioOctetCount[0] += buffer.length;
                } else {
                    port = protocol.getRtpVideoPortConsumer();
                    socket = protocol.getRtpVideoSocket();
                    videoPacketCount[0]++;
                    videoOctetCount[0] += buffer.length;
                }

                dp.setPort(port);
                try {
                    socket.send(dp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


}