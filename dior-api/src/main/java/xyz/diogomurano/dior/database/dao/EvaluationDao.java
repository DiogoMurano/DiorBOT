package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.database.dto.EvaluationDto;

public interface EvaluationDao {

    int countEvaluations(String author, long startDate, long endDate);

    void register(EvaluationDto evaluationDto);

}
