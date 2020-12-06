package xyz.diogomurano.dior.commands.assuming;

public interface Assuming {

    String getUser();

    long getStartDate();

    long getFinishDate();

    AssumingType getType();

}