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

    private static void createProtocolMap() {
        protocolMap = new HashMap<>();
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

    public static void addProtocol(String name, Protocol protocol) {
        if (protocolMap == null) {
            createProtocolMap();
        }
        protocolMap.put(name, protocol);
    }

    public static Protocol getProtocolByName(String name) {
        if (protocolMap == null) {
            createProtocolMap();
        }
        if (protocolMap.containsKey(name)) {
            return protocolMap.get(name);
        }
        protocolMap.put(name, new Protocol());
        return protocolMap.get(name);
    }
}
