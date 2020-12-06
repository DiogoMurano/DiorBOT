package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.database.dto.AnnotationDto;

public interface AnnotationDao {

    int countAnnotation(String target);

    void register(AnnotationDto annotationDto);

    void clearExpired();

}
