package com.greatCouturierGame;

public final class Task {
    private String id;
    private long cooldown;

    public Task(String id, long cooldown) {
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
        Task[] tasks = new Task[4];
        tasks[0] = new Task("1", 602000);
        tasks[1] = new Task("3", 3602000);
        tasks[2] = new Task("6", 14401000);
        tasks[3] = new Task("10", 28801000);

        return tasks;
    }
}
