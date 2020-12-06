package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.collaborator.Collaborator;

import java.util.Optional;

public interface CollaboratorDao {

    Optional<Collaborator> findByHabboName(String habboName);

    Optional<Collaborator> findByDiscordId(String discordId);

    void createOrUpdate(Collaborator collaborator);

    void delete(Collaborator collaborator);

}
