package xyz.diogomurano.dior.database.dao;

import xyz.diogomurano.dior.database.dto.PromotionDto;

public interface PromotionDao {

    int countEvaluations(String author, long startDate, long endDate);

    void register(PromotionDto promotionDto);

    void registerGM();

}
