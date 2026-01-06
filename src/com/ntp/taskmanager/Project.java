package com.ntp.taskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Birden fazla görevi barındıran proje sınıfıdır.
 */
public class Project {

    private final String id = UUID.randomUUID().toString();
    private String name;

    private final List<Task> tasks = new ArrayList<>();

    public Project(String name) {
        this.name = name;
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(Task task) {
        if (task == null) return;
        tasks.add(task);
    }

    
    public boolean removeTaskById(String taskId) {
        if (taskId == null || taskId.isBlank()) return false;
        return tasks.removeIf(t -> taskId.equals(t.getId()));
    }
}
