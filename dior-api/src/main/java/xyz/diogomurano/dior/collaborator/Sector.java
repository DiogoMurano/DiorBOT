package xyz.diogomurano.dior.collaborator;

public enum Sector {

    INITIAL("708814940546400308"),
    EVALUATIVE("708815332659298345"),
    ORGANIZATIONAL("708826135386914937"),
    PROMOTIONAL("708815040999850014"),
    ADMINISTRATIVE("708827032431362098"),
    MANAGEMENT("708534328178769971"),
    PRESIDENCY("709602964213465108"),
    FOUNDATION("708816697968033854"),
    FEDERATION("722654486967091260");

    private final String roleId;

    Sector(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }
}
