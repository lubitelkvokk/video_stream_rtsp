package protocol;

import lombok.Data;
import lombok.Getter;
import org.jnetpcap.packet.JPacket;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Data
public class Protocol {
    private static final String VERSION = " RTSP/1.0\r\n";
    private static final String RTSP_OK = "RTSP/1.0 200 OK";
    private static final String USER_AGENT = "CustomUA/1.0 (Windows 10)";

    private int rtpVideoPort;
    private int rtpAudioPort;

    private int rtcpVideoPort;
    private int rtcpAudioPort;

    private DatagramSocket socket;
    private InetAddress consumerIp;
}
