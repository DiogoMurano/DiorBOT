package xyz.diogomurano.dior.commands.assuming;

import java.util.HashMap;
import java.util.Map;

public class AssumingServiceImpl implements AssumingService {

    private final Map<AssumingType, Assuming> map;

    public AssumingServiceImpl() {
        map = new HashMap<>();
    }

    @Override
    public Assuming get(AssumingType type) {
        return map.get(type);
    }

    @Override
    public Assuming findByUser(String user) {
        return map.values().stream().filter(assuming -> assuming.getUser().equals(user)).findAny().orElse(null);
    }

    @Override
    public void put(AssumingType type, Assuming assuming) {
        this.map.put(type, assuming);
    }

    @Override
    public void remove(AssumingType type) {
        this.map.remove(type);
    }
}
