package xyz.diogomurano.dior.collaborator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class Point {

    private String habboName;
    private int points;

    public void addPoints(int points) {
        this.points += points;
    }

    public void removePoints(int points) {
        this.points -= points;
        if(this.points < 0) {
            this.points = 0;
        }
    }

}
