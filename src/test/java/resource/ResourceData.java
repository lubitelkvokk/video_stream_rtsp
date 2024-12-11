package resource;

import lombok.Data;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.voip.Rtp;

import java.util.List;

@Data
public class ResourceData {
    private String filename;
    private Double duration;
    private List<JPacket> packetList;
    private String describeArea;
}
