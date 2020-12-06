package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.collaborator.Point;

import java.util.List;

public interface PointDao {

    Point findByName(String name);

    void register(Point point);

    List<Point> selectTopPoints();

}
