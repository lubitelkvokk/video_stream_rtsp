import java.util.regex.*;

public class PCAPParser {

    // Регулярные выражения для поиска нужных данных
    private static final Pattern ETH_PATTERN = Pattern.compile("Eth:\\s*(.*)");
    private static final Pattern IP_PATTERN = Pattern.compile("Ip:\\s*(.*)");
    private static final Pattern UDP_PATTERN = Pattern.compile("Udp:\\s*(.*)");
    private static final Pattern RTP_PATTERN = Pattern.compile("RTP:\\s*(.*)");
    private static final Pattern RTCP_PATTERN = Pattern.compile("RTCP:\\s*(.*)");

    public static String parsePacket(String packet) {


        if (isRTCPPacket(packet)) return "RTCP";
        if (isRTPPacket(packet)) return "RTP";
        return "Unknown packet";
    }

    // Метод для извлечения порта из строки UDP
    private static int[] extractPort(String packetInfo) {
        String udp = packetInfo.split("Udp offset")[1];
        Pattern sourcePattern = Pattern.compile("source = (%d)+");
        Pattern destinationPattern = Pattern.compile("destination = (%d)+");
        Matcher sourceMatcher = sourcePattern.matcher(udp);
        Matcher destinationMatcher = destinationPattern.matcher(udp);
        return new int[]{Integer.parseInt(sourceMatcher.group()), Integer.parseInt(destinationMatcher.group())};
    }

    // Проверка на RTP по портам
    private static boolean isRTPPacket(String packet) {
        return packet.contains("Rtp");
    }

    // Проверка на RTCP по портам
    private static boolean isRTCPPacket(String packet) {
        return packet.contains("RTCP");
    }

    // Разделение RTP на аудио и видео
    public static boolean isAudioRTP(String packet) {
        return packet.contains("type = 97"); // type = 96 for video
    }

    public static void main(String[] args) {
        // Пример строки пакета
        String packet = "Eth: ******* Ethernet - ... Ip: ******* Ip4 - ... Udp: source = 54946 destination = 554 ... RTP: ... RTCP: ...";

        // Парсим пакет
        parsePacket(packet);
    }
}
