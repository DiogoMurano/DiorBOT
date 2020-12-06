package xyz.diogomurano.dior.api.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class HabboUser {

    private String uniqueId;
    private String name;
    private String figureString;
    private String motto;
    private Date memberSince;
    private boolean profileVisible;

    private List<Badge> selectedBadges;
}
