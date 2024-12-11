import protocol.Protocol;
import protocol.RequestType;
import resource.ResourceMapping;
import resource.exceptions.NotFoundResourceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static protocol.RTSPUtil.*;

public class Server {
    private static final String IPV4 = "127.0.0.1";
    private static final int serverTcpPort = 5554;
    private static final String serverName = "Java custom RTSP server";
    private static final Integer rtpAudioPort = 49188;
    private static final Integer rtcpAudioPort = 49189;
    private static final Integer rtpVideoPort = 49190;
    private static final Integer rtcpVideoPort = 49191;

    public void start(int port) {
        System.out.println("RTSP Server is starting...");
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("RTSP Server is listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException | NotFoundResourceException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void handleClient(Socket clientSocket) throws IOException, NotFoundResourceException {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            System.out.println("New client connected: " + clientSocket.getInetAddress());
            String sessionId = generateSessionId();
            Protocol protocol = new Protocol();
            protocol.setConsumerIp(clientSocket.getInetAddress());
            ResourceMapping.addProtocol(sessionId, protocol);
            String requestLine;
            while ((requestLine = in.readLine()) != null) {
                System.out.println("Received: " + requestLine);
                if (requestLine.startsWith(RequestType.OPTIONS.toString())) {
                    String cseq = parseCSeq(in);
                    sendOptionsResponse(out, cseq);
                } else if (requestLine.startsWith(RequestType.DESCRIBE.toString())) {
                    String resourceName = parseResource(requestLine);
                    String cseq = parseCSeq(in);
                    sendDescribeResponse(out, cseq, resourceName, sessionId);
                } else if (requestLine.startsWith(RequestType.SETUP.toString())) {
                    String resourceName = parseResource(requestLine);
                    String cseq = parseCSeq(in);
                    String RTP_AND_RTCP_PORTS = parseClientPort(in);
                    sendSetupResponse(out, cseq, resourceName, RTP_AND_RTCP_PORTS, sessionId);
                } else if (requestLine.startsWith(RequestType.PLAY.toString())) {
                    String resourceName = parseResource(requestLine);
                    String cseq = parseCSeq(in);
                    sendPlayResponse(out, cseq, resourceName, sessionId);
                } else if (requestLine.startsWith(RequestType.TEARDOWN.toString())) {
                    String cseq = parseCSeq(in);
                    sendTeardownResponse(out, cseq, sessionId);
                    break;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendOptionsResponse(PrintWriter out, String cseq) {
        String response = "RTSP/1.0 200 OK\r\n" +
                "CSeq: " + cseq + "\r\n" +
                "Public: OPTIONS, DESCRIBE, SETUP, PLAY, PAUSE, TEARDOWN\r\n\r\n";
        out.print(response);
        out.flush();
        System.out.println("Sent OPTIONS response.");
    }

    private static void sendDescribeResponse(PrintWriter out, String cseq, String resourceName, String sessionId) throws NotFoundResourceException {
        String sdp = "v=0\r\n" +
                "o=- " + sessionId + " " + sessionId + " IN IP4 " + IPV4 + "\r\n" +
                "s=" + resourceName + "\r\n" +
                "c=IN IP4 " + IPV4 + "\r\n" +
                "t=0 0\r\n" +
                "a=sdplang:en\r\n" +
                "a=range:npt=0-" + ResourceMapping.getResourceByName(resourceName).getDuration() + "\r\n" +
                "a=control:*\r\n" +
                ResourceMapping.getResourceByName(resourceName).getDescribeArea();

        String response = "RTSP/1.0 200 OK\r\n" +
                "CSeq: " + cseq + "\r\n" +
                "Content-Base: rtsp://" + IPV4 + ":" + serverTcpPort + "/" + resourceName + "\r\n" +
                "Content-Type: application/sdp\r\n" +
                "Content-Length: " + sdp.length() + "\r\n\r\n" +
                sdp;
        out.print(response);
        out.flush();
        System.out.println("Sent DESCRIBE response.");
    }

    private static int[] parsePorts(String RTP_AND_RTCP_PORTS) {
        String[] rtpAndRtcp = RTP_AND_RTCP_PORTS.split("-");
        return new int[]{Integer.parseInt(rtpAndRtcp[0]),
                Integer.parseInt(rtpAndRtcp[1])};
    }

    private static void sendSetupResponse(PrintWriter out, String cseq, String resourceName, String RTP_AND_RTCP_PORTS, String sessionId) throws SocketException {
        System.out.println("SETUP PORTS: " + RTP_AND_RTCP_PORTS);
        String ssrc = Util.randomHexString(8);
        String setupResponse;
        int[] rtpAndRtcp = parsePorts(RTP_AND_RTCP_PORTS);
        // always not null
        int[] chosenServerPorts = new int[2];
        Protocol protocol = ResourceMapping.getProtocolByName(sessionId);
        if (resourceName.equals("trackID=1")) {
            protocol.setRtcpAudioSSRC(ssrc);
            protocol.setRtpAudioPortConsumer(rtpAndRtcp[0]);
            protocol.setRtcpAudioPortConsumer(rtpAndRtcp[1]);
            protocol.setRtpAudioSocket(new DatagramSocket(rtpAudioPort));
            protocol.setRtcpAudioSocket(new DatagramSocket(rtcpAudioPort));
            chosenServerPorts[0] = rtpAudioPort;
            chosenServerPorts[1] = rtcpAudioPort;
        } else {
            protocol.setRtcpVideoSSRC(ssrc);
            protocol.setRtpVideoPortConsumer(rtpAndRtcp[0]);
            protocol.setRtcpVideoPortConsumer(rtpAndRtcp[1]);
            protocol.setRtpVideoSocket(new DatagramSocket(rtpVideoPort));
            protocol.setRtcpVideoSocket(new DatagramSocket(rtcpVideoPort));
            chosenServerPorts[0] = rtpVideoPort;
            chosenServerPorts[1] = rtcpVideoPort;
        }
        setupResponse =
                "RTSP/1.0 200 OK\r\n" +
                        "CSeq: " + cseq + "\r\n" +
                        "Server:" + serverName + "\r\n" +
                        "Cache-Control: no-cache\r\n" +
                        "Transport: RTP/AVP;unicast;client_port=" + RTP_AND_RTCP_PORTS + ";source=" + protocol.getConsumerIp() + ";server_port=" + chosenServerPorts[0] + "-" + chosenServerPorts[1] + ";ssrc=" + ssrc + "\r\n" +
                        "Date: " + new Date() + "\r\n" +
                        "Session: " + sessionId + "\r\n" +
                        "\r\n";

        ResourceMapping.addProtocol(sessionId, protocol);
        out.print(setupResponse);
        out.flush();
        System.out.println("Sent SETUP response.");
    }

    private static void sendPlayResponse(PrintWriter out, String cseq, String resourceName, String sessionId) throws NotFoundResourceException, InterruptedException {
        String playResponse =
                "RTSP/1.0 200 OK\r\n" +
                        "Server: " + serverName + "\r\n" +
                        "CSeq: " + cseq + "\r\n" +
                        "RTP-Info: " +
                        "url=rtsp://" + IPV4 + ":" + serverTcpPort + "/" + resourceName + "/trackID=1;" +
                        "seq=1;" +
                        "rtptime=0,url=rtsp://" + IPV4 + ":" + serverTcpPort + "/" + resourceName + "/trackID=2;" +
                        "seq=1;" +
                        "rtptime=0\r\n" +
                        "Cache-Control: no-cache\r\n" +
                        "Range: npt=0.0-" + ResourceMapping.getResourceByName(resourceName).getDuration() + "\r\n" +
                        "Session: " + sessionId + "; timeout=60\r\n" +
                        "\r\n";


        out.print(playResponse);
        out.flush();
        PCAPProcessor.processPCAP(ResourceMapping.getResourceByName(resourceName),
                ResourceMapping.getProtocolByName(sessionId));
    }

    private static void sendTeardownResponse(PrintWriter out, String cseq, String sessionId) {
        String response = "RTSP/1.0 200 OK\r\n" +
                "CSeq: " + cseq + "\r\n" +
                "Session: " + sessionId + "\r\n\r\n";
        out.print(response);
        out.flush();
        System.out.println("Sent TEARDOWN response.");
    }

}
