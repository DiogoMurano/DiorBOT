package xyz.diogomurano.dior.collaborator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public enum Role {

    TRAINEE("Estagiário", Sector.INITIAL, 2, "708842424969199627", "SUB_MANAGER"),
    MODEL_LEADER("Líder de Modelos", Sector.INITIAL, 2, "708842515788464219", "SUB_MANAGER"),
    SUPERVISOR("Supervisor", Sector.EVALUATIVE, 4, "708842610009309206", "ADMINISTRATOR"),
    COORDINATOR("Coordenador", Sector.EVALUATIVE, 5, "708842614362734612", "ADMINISTRATOR"),
    ANALYST("Analista", Sector.ORGANIZATIONAL, 6, "708842746114474015", "ADMINISTRATOR"),
    ADVISOR("Orientador", Sector.ORGANIZATIONAL, 6, "708842748031008778", "ADMINISTRATOR"),
    SUB_MANAGER("Sub Gerente", Sector.PROMOTIONAL, 10, "708842917439209543", "ADMINISTRATOR"),
    MANAGER("Gerente", Sector.PROMOTIONAL, 10, "708842915828596796", "ADMINISTRATOR"),
    ADMINISTRATOR("Administrador", Sector.ADMINISTRATIVE, 10, "708843035080786012", "SUPERINTENDENT"),
    SUPERINTENDENT_APPRENTICE("Aprendiz de Superintendente", Sector.ADMINISTRATIVE, 10, "708843040185516064", "CFO"),
    SUPERINTENDENT("Superintendente", Sector.ADMINISTRATIVE, 10, "708843049953919037", "CFO"),
    EVENTS_DIRECTOR("Diretor de Eventos", Sector.MANAGEMENT, -1, "721205610309353632", "OWNER"),
    CFO("Diretor Financeiro", Sector.MANAGEMENT, -1, "708843244804374629", "GENERAL_DIRECTOR"),
    HR_DIRECTOR("Diretor de RH", Sector.MANAGEMENT, -1, "708843250789908541", "GENERAL_DIRECTOR"),
    GENERAL_DIRECTOR("Diretor Geral", Sector.MANAGEMENT, -1, "708843252765163795", "PRESIDENT"),
    VICE_PRESIDENT("Vice Presidente", Sector.PRESIDENCY, -1, "708843474669142088", "PRESIDENT"),
    PRESIDENT("Presidente", Sector.PRESIDENCY, -1, "708843479178018906", "SUB_OWNER"),
    SUB_FOUNDER("Aprendiz de Fundador", Sector.FOUNDATION, -1, "714477942960422993", "SUB_OWNER"),
    FOUNDER("Fundador", Sector.FOUNDATION, -1, "708535132134309919", "SUB_OWNER"),
    CHANCELER("Chanceler", Sector.FEDERATION, -1, "722654317877919747", "OWNER"),
    SUB_OWNER("Sub Dono", Sector.FEDERATION, -1, "722644878953152533", "OWNER"),
    OWNER("Dono", Sector.FEDERATION, -1, "708538357445165076", "OWNER");

    private final String name;
    private final Sector sector;
    private final int daysToPromote;
    private final String roleId;
    private final String promoterName;

    Role(String name, Sector sector, int daysToPromote, String roleId, String promoterName) {
        this.name = name;
        this.sector = sector;
        this.daysToPromote = daysToPromote;
        this.roleId = roleId;
        this.promoterName = promoterName;
    }

    public String getName() {
        return name;
    }

    public Sector getSector() {
        return sector;
    }

    public int getDaysToPromote() {
        return daysToPromote;
    }

    public String getRoleId() {
        return roleId;
    }

    public int getId() {
        return ordinal();
    }

    public Role getPromoter() {
        return findByOriginalName(promoterName).orElse(null);
    }

    public boolean isUnder(Role role) {
        return ordinal() < role.getId();
    }

    public static Role getDefaultRole() {
        return TRAINEE;
    }

    public boolean isLowLevel() {
        return ordinal() == TRAINEE.ordinal() || ordinal() == MODEL_LEADER.ordinal();
    }

    public static Optional<Role> findById(int id) {
        Role role = null;
        for (Role roles : values()) {
            if (roles.getId() == id) {
                role = roles;
                break;
            }
        }
        return Optional.ofNullable(role);
    }

    public static Optional<Role> findByName(String roleName) {
        Role role = null;
        for (Role roles : values()) {
            if (roles.getName().equalsIgnoreCase(roleName)) {
                role = roles;
                break;
            }
        }
        return Optional.ofNullable(role);
    }

    public static Optional<Role> findByOriginalName(String roleName) {
        Role role = null;
        for (Role roles : values()) {
            if (roles.name().equalsIgnoreCase(roleName)) {
                role = roles;
                break;
            }
        }
        return Optional.ofNullable(role);
    }

    public static Set<Role> getAllBelow(Role role) {
        Set<Role> roleList = new HashSet<>();
        for (Role roles : values()) {
            if (role.ordinal() > roles.ordinal()) {
                roleList.add(roles);
            }
        }
        return roleList;
    }

}
