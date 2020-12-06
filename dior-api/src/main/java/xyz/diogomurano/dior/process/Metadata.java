package xyz.diogomurano.dior.process;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class Metadata {

    private final Map<String, Object> storedObjects;

    public Metadata() {
        this.storedObjects = Collections.synchronizedMap(new WeakHashMap<>());
    }

    public boolean containsKey(String key) {
        return storedObjects.containsKey(key);
    }
 
    public <T> boolean containsValue(T value) {
        return storedObjects.containsValue(value);
    }

    public <T> void store(String key, T value) {
        storedObjects.put(key, value);
    }

    public void remove(String key) {
        storedObjects.remove(key);
    }

    public <T> T get(String key) {
        return (T) storedObjects.get(key);
    }


}
