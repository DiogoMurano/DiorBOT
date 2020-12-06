package xyz.diogomurano.dior.database.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InterviewDto {

    private final String author;
    private final String target;
    private final int finalNote;
    private final long date;

}
