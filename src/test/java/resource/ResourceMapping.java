package resource;

import protocol.Protocol;
import resource.exceptions.NotFoundResourceException;

import java.util.HashMap;
import java.util.Map;

public class ResourceMapping {
    private static Map<String, ResourceData> resourceDataMap;
    private static Map<String, Protocol> protocolMap; // mapping by ssrc

    private static void createResourceDataMap() {
        resourceDataMap = new HashMap<>();
    }

    public static ResourceData getResourceByName(String name) throws NotFoundResourceException {
        if (resourceDataMap == null) {
            createResourceDataMap();
        }

        if (resourceDataMap.containsKey(name)) {
            return resourceDataMap.get(name);
        }
        throw new NotFoundResourceException();
    }

    public static void addResource(String name, ResourceData resourceData) throws NotFoundResourceException {
        if (resourceDataMap == null) {
            createResourceDataMap();
        }
        resourceDataMap.put(name, resourceData);
    }
}
