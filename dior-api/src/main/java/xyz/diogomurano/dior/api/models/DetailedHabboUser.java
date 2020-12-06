package xyz.diogomurano.dior.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DetailedHabboUser {


    private String uniqueId;
    private String name;
    private String figureString;
    private String motto;
    private Date memberSince;
    private boolean profileVisible;

    private List<Badge> selectedBadges;
    private List<Friend> friends;
    private List<Group> groups;
    private List<Room> rooms;
    private List<TotalBadge> badges;


}
