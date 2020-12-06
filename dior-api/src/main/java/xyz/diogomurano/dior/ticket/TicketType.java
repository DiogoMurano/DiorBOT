package xyz.diogomurano.dior.ticket;

public enum TicketType {

    HIRING("Contratação"),
    PROMOTION("Promoção"),
    EVALUATION("Avaliação"),
    INTERVIEW("Entrevista"),
    SATISFACTION_INTERVIEW("Entrevista"),
    DEMOTE("Demissão"),
    ANNOTATION("Anotação"),
    SUGGESTION("Sugestão"),
    RECLAMATION("Reclamação"),
    DOUBT("Dúvida"),
    REPORT("Relatório"),
    OTHERS("Outros");

    private final String name;

    TicketType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
