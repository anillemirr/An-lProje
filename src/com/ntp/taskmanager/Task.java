package com.ntp.taskmanager;

import java.util.Objects;
import java.util.UUID;

/**
 * Sistemdeki temel görev (Task) sınıfıdır.
 *
 * <p>
 * Her görev bir başlık, açıklama, teslim tarihi (Deadline)
 * ve öncelik seviyesi (Priority) içerir.
 * </p>
 *
 * <p>
 * Deadline ve Priority alanları kapsülleme (encapsulation)
 * prensibine uygun olarak private tutulmuştur.
 * </p>
 *
 * <p>
 * Bu sınıf {@link Completable} arayüzünü uygular.
 * </p>
 */
public class Task implements Completable {

    private final String id = UUID.randomUUID().toString();

    private String title;
    private String description;

    // Encapsulation
    private Deadline deadline;
    private Priority priority;

    private boolean completed;

    public Task(String title, String description, Deadline deadline, Priority priority) {
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.deadline = Objects.requireNonNull(deadline);
        this.priority = Objects.requireNonNull(priority);
        this.completed = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Deadline getDeadline() {
        return deadline;
    }

    public void setDeadline(Deadline deadline) {
        this.deadline = Objects.requireNonNull(deadline);
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority);
    }

    @Override
    public void complete() {
        this.completed = true;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id.substring(0, 8) + '\'' +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", deadline=" + deadline +
                ", completed=" + completed +
                '}';
    }
}
