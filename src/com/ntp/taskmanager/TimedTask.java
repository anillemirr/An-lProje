package com.ntp.taskmanager;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Zaman aralığına (start/end) sahip görevleri temsil eder.
 *
 * <p>
 * {@code TimedTask}, {@link Task} sınıfından türetilmiştir.
 * Bu sayede kalıtım (inheritance) gereksinimi sağlanır.
 * </p>
 */
public class TimedTask extends Task {

    private LocalDateTime start;
    private LocalDateTime end;

    public TimedTask(String title,
                     String description,
                     Deadline deadline,
                     Priority priority,
                     LocalDateTime start,
                     LocalDateTime end) {
        super(title, description, deadline, priority);

        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End cannot be before start.");
        }
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = Objects.requireNonNull(start);
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = Objects.requireNonNull(end);
        if (this.end.isBefore(this.start)) {
            throw new IllegalArgumentException("End cannot be before start.");
        }
    }

    @Override
    public String toString() {
        return super.toString() + " (Timed: " + start + " -> " + end + ")";
    }
}
