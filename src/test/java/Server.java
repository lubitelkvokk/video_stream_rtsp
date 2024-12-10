import resource.ResourceMapping;
import resource.exceptions.NotFoundResourceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String IPV4 = "127.0.0.1";

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

    // Извлечение URL ресурса из строки запроса
    private static String parseResource(String requestLine) {
        String[] parts = requestLine.split(" ")[1].split("/");
        return parts[parts.length - 1];
    }


    private static String parseCSeq(BufferedReader in) throws IOException {
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            if (headerLine.startsWith("CSeq:")) {
                return headerLine.split(":")[1].trim(); // Извлекаем номер CSeq
            }
        }
        return null;
    }

    private static String parseClientPort(BufferedReader in) throws IOException {
        String clientPorts;
        while ((clientPorts = in.readLine()) != null) {
            if (clientPorts.contains("client_port")) {
                clientPorts = clientPorts.split("client_port=")[1];
                return clientPorts;
            }
        }
        return null;
    }

    private static void handleClient(Socket clientSocket) throws IOException, NotFoundResourceException {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            System.out.println("New client connected: " + clientSocket.getInetAddress());
            String sessionId = generateSessionId();
            String requestLine;
            while ((requestLine = in.readLine()) != null) {
                System.out.println("Received: " + requestLine);

//                if (requestLine.isEmpty()) break; // Конец запроса

//                // Разбираем первый запрос
                if (requestLine.startsWith("OPTIONS")) {
                    String cseq = parseCSeq(in);
                    sendOptionsResponse(out, cseq);
                } else if (requestLine.startsWith("DESCRIBE")) {
                    String resourceName = parseResource(requestLine);
                    String cseq = parseCSeq(in);
                    sendDescribeResponse(out, cseq, resourceName, sessionId);
                } else if (requestLine.startsWith("SETUP")) {
                    String resourceName = parseResource(requestLine);
                    String cseq = parseCSeq(in);
                    String RTP_AND_RTCP_PORTS = parseClientPort(in);
                    sendSetupResponse(out, cseq, resourceName, RTP_AND_RTCP_PORTS);
                } else if (requestLine.startsWith("PLAY")) {
                    String resourceName = parseResource(requestLine);
                    String cseq = parseCSeq(in);
                    sendPlayResponse(out, cseq, resourceName, sessionId);

                }
            }
        }
    }

    private static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
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
                "o=- " + sessionId + " " + 1 + " IN IP4 " + IPV4 + "\r\n" +
                "s= CUSTOM JAVA RTSP SERVER\r\n" +
                "c=IN IP4 " + IPV4 + "\r\n" +
                "t=0 0\r\n" +
                "a=sdplang:en\r\n" +
                "a=range:npt=0-" + ResourceMapping.getResourceByName(resourceName).getDuration() + "\r\n" +
                "a=control:*\r\n" +
                "m=audio 0 RTP/AVP 96\r\n" +
                "a=rtpmap:96 mpeg4-generic/12000/2\r\n" +
                "a=control:trackID=1\r\n" +
                "m=video 0 RTP/AVP 97\r\n" +
                "a=rtpmap:97 H264/90000\r\n" +
                "a=control:trackID=2\r\n";

        String response = "RTSP/1.0 200 OK\r\n" +
                "CSeq: " + cseq + "\r\n" +
                "Content-Base: rtsp://127.0.0.1:5554/" + resourceName + "\r\n" +
                "Content-Type: application/sdp\r\n" +
                "Content-Length: " + sdp.length() + "\r\n\r\n" +
                sdp;
        out.print(response);
        out.flush();
        System.out.println("Sent DESCRIBE response.");
    }

    private static void sendSetupResponse(PrintWriter out, String cseq, String resourceName, String RTP_AND_RTCP_PORTS) {
        System.out.println("SETUP PORTS: " + RTP_AND_RTCP_PORTS);
        String setupResponse =
                "RTSP/1.0 200 OK\r\n" +
                        "CSeq: " + cseq + "\r\n" +
                        "Server: Wowza Streaming Engine 4.7.5.01 build21752\r\n" +
                        "Cache-Control: no-cache\r\n" +
                        "Transport: RTP/AVP;unicast;client_port=" + RTP_AND_RTCP_PORTS + ";source=127.0.0.1;server_port=10002-10003" +
                        "Date: Sun, 26 Apr 2020 07:36:42 UTC\r\n" +
                        "Session: 1\r\n" +
                        "\r\n";
        out.print(setupResponse);
        out.flush();
        System.out.println("Sent SETUP response.");
    }
    private static void sendPlayResponse(PrintWriter out, String cseq, String resourceName, String sessionId) throws NotFoundResourceException {
        String playResponse = "RTSP/1.0 200 OK\r\n" +
                "CSeq: " + cseq + "\r\n" +
                "Date: Fri, Apr 23 2010 19:54:20 GMT\r\n" +
                "Range: npt=0.000-\r\n" +
                "Session: 1\r\n" +
                "RTP-Info: " +
                "url=rtsp://127.0.0.1:5554/" + resourceName + "/trackID=1;" +
                "seq=1;" +
                "rtptime=0,url=rtsp://127.0.0.1:5554/" + resourceName + "/trackID=2;" +
                "seq=1;" +
                "rtptime=0\\r\\n";
        out.print(playResponse);
        out.flush();

//        List<Packet> packets =
//                ResourceMapping.getResourceByName(resourceName).getPacketList();
//        for (Packet p: packets){
//            out.print(p);
//            out.flush();
//        }
    }

    private static void sendErrorResponse(PrintWriter out) {

    }

}
