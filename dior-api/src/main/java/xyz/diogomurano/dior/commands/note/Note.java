package xyz.diogomurano.dior.commands.note;

public class Note {

    private final String target;
    private final NoteType type;
    private final long date;

    public Note(String target, NoteType type, long date) {
        this.target = target;
        this.type = type;
        this.date = date;
    }

    public String getTarget() {
        return target;
    }

    public NoteType getType() {
        return type;
    }

    public long getDate() {
        return date;
    }
}
