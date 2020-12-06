package xyz.diogomurano.dior.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TotalBadge {

    private String code;
    private String name;
    private String description;

}
