package xyz.diogomurano.dior.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Group {

    private String id;
    private String name;
    private String description;
    private String type;
    private String badgeCode;
    private String roomId;
    private String primaryColour;
    private String secondaryColour;
    private boolean isAdmin;

}
