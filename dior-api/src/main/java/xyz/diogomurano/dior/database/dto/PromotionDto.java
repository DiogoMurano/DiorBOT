package xyz.diogomurano.dior.database.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.diogomurano.dior.collaborator.Role;

@AllArgsConstructor
@Getter
public class PromotionDto {

    private final String author;
    private final String target;
    private final Role role;
    private final long date;

}
