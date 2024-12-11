package resource;

import lombok.Data;
import org.jnetpcap.protocol.voip.Rtp;

import java.util.List;

@Data
public class ResourceData {
    private String filename;
    private Double duration;
    private List<Rtp> packetList;
}
