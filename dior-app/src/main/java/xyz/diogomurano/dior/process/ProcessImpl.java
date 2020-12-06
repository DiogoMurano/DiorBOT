package xyz.diogomurano.dior.process;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ProcessImpl implements Process {

    private final Set<Step> steps;
    private int current;

    public ProcessImpl() {
        steps = new HashSet<>();
        current = 0;
    }

    @Override
    public Set<Step> getSteps() {
        return steps;
    }

    @Override
    public void add(Step step) {
        Objects.requireNonNull(step, "step can't be null.");
        this.steps.add(step);
    }

    @Override
    public void remove(Step step) {
        Objects.requireNonNull(step, "step can't be null.");
        this.steps.remove(step);
    }

    @Override
    public Step findByName(int id) {
        return steps.stream().filter(step -> step.getId() == id).findAny().orElse(null);
    }

    @Override
    public Step findById(int id) {
        return steps.stream().filter(step -> step.getId() == id).findAny().orElse(null);
    }

    @Override
    public Step findCurrent() {
        return steps.stream().filter(step -> step.getId() == current).findAny().orElse(null);
    }

    @Override
    public void setCurrent(Step step) {
        Objects.requireNonNull(step, "step can't be null.");
        this.current = step.getId();
    }

    @Override
    public void sendMessage(TextChannel channel) {
        final Step step = findById(current);
        channel.sendMessage(step.getMessage()).queue(m -> {
            if (step.isReact()) {
                addReaction(m, "✅");
                addReaction(m, "❌");
            } else if (step.isRating()) {
                addReaction(m, "1️⃣");
                addReaction(m, "2️⃣");
                addReaction(m, "3️⃣");
                addReaction(m, "4️⃣");
                addReaction(m, "5️⃣");
            }
        });
    }

    private void addReaction(Message message, String reaction) {
        if (message != null) {
            try {
                message.addReaction(reaction).queue();
            } catch (Exception ignored) {
            }
        }
    }

}
