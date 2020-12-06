package xyz.diogomurano.dior.database.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EvaluationDto {

    private final String author;
    private final String target;
    private final double finalNote;
    private final long date;

}
