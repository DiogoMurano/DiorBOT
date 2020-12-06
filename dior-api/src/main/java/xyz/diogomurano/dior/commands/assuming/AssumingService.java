package xyz.diogomurano.dior.commands.assuming;

public interface AssumingService {

    Assuming get(AssumingType type);

    Assuming findByUser(String user);

    void put(AssumingType type, Assuming assuming);

    void remove(AssumingType type);

}
