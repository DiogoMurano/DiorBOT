package xyz.diogomurano.dior.timer;

import xyz.diogomurano.dior.BotManager;
import xyz.diogomurano.dior.collaborator.Point;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PointTimer {

    private BotManager botManager;

    private final static ScheduledExecutorService POOL = Executors.newSingleThreadScheduledExecutor();

    public PointTimer(BotManager botManager) {
        this.botManager = botManager;
    }

    public void start() {
        POOL.scheduleWithFixedDelay(this::execute, 0, 60, TimeUnit.DAYS);
    }

    public void execute() {
        final List<Point> points = botManager.getPointDao().selectTopPoints();

        StringBuilder builder = new StringBuilder();

        builder.append("**RANKEAMENTO - ATUAL**").append("\n\n");

    }

}
