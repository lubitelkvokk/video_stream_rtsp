package protocol;

import org.jnetpcap.protocol.voip.Rtp;

public class RtpProcessor {
    public static byte[] createRtpPacket() {
        byte[] packet = new byte[12 + 160]; // Заголовок RTP (12 байт) + полезная нагрузка (160 байт)

        // Пример заголовка RTP (в реальной ситуации нужно заполнять поля, такие как Sequence Number, Timestamp и SSRC)
        packet[0] = (byte) 0x80;  // Версия RTP и другие флаги
        packet[1] = (byte) 0x21;  // Тип полезной нагрузки
        packet[2] = (byte) 0x00;  // Номер последовательности (Sequence Number)
        packet[3] = (byte) 0x01;

        // Дальше идут другие поля заголовка и полезная нагрузка
        // В реальной ситуации вы должны установить все поля правильно (например, Sequence Number, Timestamp, SSRC)

        return packet;
    }
}

