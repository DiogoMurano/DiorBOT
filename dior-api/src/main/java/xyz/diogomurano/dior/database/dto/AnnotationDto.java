package xyz.diogomurano.dior.database.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AnnotationDto {

    private final String author;
    private final String target;
    private final String reason;
    private final long date;

}
