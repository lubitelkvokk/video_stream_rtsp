package protocol.rtcp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class RTCPProcessor {

    public static void sendSenderReport(DatagramSocket rtcpSocket, InetAddress clientAddress, int clientPort, String ssrcHex, long packetCount, long octetCount) {
        try {
            // Преобразование SSRC из строки в 32-битное беззнаковое число
            long ssrc = convertSsrc(ssrcHex);

            ByteBuffer buffer = ByteBuffer.allocate(28); // Размер SR пакета

            // Заголовок RTCP
            buffer.put((byte) 0x80); // Version 2, Padding 0, Report Count 0
            buffer.put((byte) 200); // Sender Report (SR)
            buffer.putShort((short) 6); // Длина пакета (в словах по 32 бита, без заголовка RTCP)

            // Поле SSRC
            buffer.putInt((int) ssrc); // SSRC источника

            // NTP Timestamp (64 бита)
            long ntpSec = System.currentTimeMillis() / 1000; // секунды с 1970
            long ntpFrac = ((System.currentTimeMillis() % 1000) * 0xFFFFFFFFL) / 1000; // дробная часть
            buffer.putInt((int) ntpSec);
            buffer.putInt((int) ntpFrac);

            // RTP Timestamp (32 бита)
            buffer.putInt((int) (System.currentTimeMillis() * 90)); // Пример: RTP Timestamp

            // Отправленные пакеты и байты
            buffer.putInt((int) packetCount); // Количество отправленных RTP пакетов
            buffer.putInt((int) octetCount); // Количество отправленных байтов

            // Отправка RTCP пакета
            DatagramPacket dp = new DatagramPacket(buffer.array(), buffer.position(), clientAddress, clientPort);
            rtcpSocket.send(dp);

            System.out.println("Sent RTCP Sender Report for SSRC: " + ssrcHex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long convertSsrc(String ssrcHex) {
        return Long.parseUnsignedLong(ssrcHex, 16); // Преобразование строки в беззнаковое число
    }

}
