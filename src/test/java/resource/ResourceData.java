package resource;

import lombok.Data;

import java.util.List;

@Data
public class ResourceData {
    private String filename;
    private Double duration;
    private List<String> packetList;
}
