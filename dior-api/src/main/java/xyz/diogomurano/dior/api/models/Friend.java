package xyz.diogomurano.dior.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Friend {

    private String name;
    private String motto;
    private String uniqueId;
    private String figureString;

}
