package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.collaborator.Credit;

public interface CreditDao {

    Credit findByName(String name);

    void register(Credit credit);

}
