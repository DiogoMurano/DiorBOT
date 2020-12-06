package xyz.diogomurano.dior.database;

public interface Crud<T> {

    void createOrUpdate(T t);

    void delete(T t);

}
