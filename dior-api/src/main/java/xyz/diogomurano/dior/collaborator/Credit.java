package xyz.diogomurano.dior.collaborator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class Credit {

    private String habboName;
    private int coins;

}
