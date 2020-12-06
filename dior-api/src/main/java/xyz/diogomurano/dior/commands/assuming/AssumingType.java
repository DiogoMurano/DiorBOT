package xyz.diogomurano.dior.commands.assuming;

public enum AssumingType {

    C1("Comando Primário"),
    C2("Comando Secundário"),
    AUX_HALL_1("Auxílio de HALL 1"),
    AUX_GENERAL("Auxílio de SEDE");

    private final String name;

    AssumingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
