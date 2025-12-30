package com.ntp.taskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final String id = UUID.randomUUID().toString();
    private String name;

    private final List<Task> myTasks = new ArrayList<>();

    public User(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    public void assignTask(Task task) {
        myTasks.add(Objects.requireNonNull(task));
    }

    public List<Task> getMyTasks() {
        return Collections.unmodifiableList(myTasks);
    }
}
