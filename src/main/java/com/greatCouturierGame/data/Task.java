package com.greatCouturierGame.data;

public final class Task {

    private String id;
    private long cooldown;

    private Task(String id, long cooldown) {
        this.id = id;
        this.cooldown = cooldown;
    }

    public String getId() {
        return id;
    }

    public long getCooldown() {
        return cooldown;
    }

    public static Task[] getAllTasks() {
        return new Task[] {
                new Task("1", 602000),
                new Task("3", 3602000),
                new Task("6", 14401000),
                new Task("10", 28801000),
                new Task("15", 57601000)
        };
    }

}
