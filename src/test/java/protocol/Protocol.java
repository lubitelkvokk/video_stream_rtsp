package protocol;

import lombok.Data;
import lombok.Getter;
import org.jnetpcap.packet.JPacket;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

@Data
public class Protocol {
    private static final String VERSION = " RTSP/1.0\r\n";
    private static final String RTSP_OK = "RTSP/1.0 200 OK";
    private static final String USER_AGENT = "CustomUA/1.0 (Windows 10)";

    private int rtpVideoPortConsumer;
    private int rtpAudioPortConsumer;

    private int rtcpVideoPortConsumer;
    private int rtcpAudioPortConsumer;

    private DatagramSocket socket;
    private InetAddress consumerIp;

    private DatagramSocket rtpVideoSocket;
    private DatagramSocket rtpAudioSocket;
    private DatagramSocket rtcpAudioSocket;
    private DatagramSocket rtcpVideoSocket;

    private String rtcpVideoSSRC;
    private String rtcpAudioSSRC;

    public Protocol(int rtpVideoPort, int rtpAudioPort, int rtcpVideoPort, int rtcpAudioPort) throws SocketException {
        rtpVideoSocket = new DatagramSocket(rtpVideoPort);
        rtpAudioSocket = new DatagramSocket(rtpAudioPort);
        rtcpVideoSocket = new DatagramSocket(rtcpVideoPort);
        rtcpAudioSocket = new DatagramSocket(rtcpAudioPort);

    }

    public Protocol() {

    }
}
