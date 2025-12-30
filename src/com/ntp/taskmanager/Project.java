package com.ntp.taskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Project {
    private final String id = UUID.randomUUID().toString();
    private String name;

    private final List<Task> tasks = new ArrayList<>();

    public Project(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    public void addTask(Task task) {
        tasks.add(Objects.requireNonNull(task));
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    @Override
    public String toString() {
        return "Project{name='" + name + "', tasks=" + tasks.size() + "}";
    }
}
