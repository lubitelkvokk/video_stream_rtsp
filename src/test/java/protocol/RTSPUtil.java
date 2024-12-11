package protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;

public class RTSPUtil {

    public static String parseResource(String requestLine) {
        String[] parts = requestLine.split(" ")[1].split("/");
        return parts[parts.length - 1];
    }


    public static String parseCSeq(BufferedReader in) throws IOException {
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            if (headerLine.startsWith("CSeq:")) {
                return headerLine.split(":")[1].trim(); // Извлекаем номер CSeq
            }
        }
        return null;
    }

    public static String parseClientPort(BufferedReader in) throws IOException {
        String clientPorts;
        while ((clientPorts = in.readLine()) != null) {
            if (clientPorts.contains("client_port")) {
                clientPorts = clientPorts.split("client_port=")[1];
                return clientPorts;
            }
        }
        return null;
    }

    public static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
