import resource.ResourceData;
import resource.ResourceMapping;
import resource.exceptions.NotFoundResourceException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, NotFoundResourceException {
        ResourceData re = new ResourceData();
        re.setFilename("file.mp4");
        re.setDuration(598.48);

        ResourceMapping.addResource("ad2e", re);
        Server server = new Server();
        server.start(5554);
    }

}