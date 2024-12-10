import resource.exceptions.NotFoundResourceException;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, NotFoundResourceException {
        List<String> result = PCAPProcessor.processPCAP("resources/bunny.pcapng");
        result.forEach(System.out::println);
//        result.forEach(System.out::println);
//        packetList.stream().map(PCAPProcessor::getProtocol).forEach(System.out::println);
//        ResourceData re = new ResourceData();
//        re.setFilename("file.mp4");
//        re.setDuration(598.48);
//        re.setPacketList(packetList);
//
//        ResourceMapping.addResource("ad2e", re);
//        Server server = new Server();
//        server.start(5554);
//
    }

}