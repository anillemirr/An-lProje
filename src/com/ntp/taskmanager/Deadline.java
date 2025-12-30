package com.ntp.taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Deadline {
    private LocalDateTime due; // encapsulated

    public Deadline(LocalDateTime due) {
        this.due = Objects.requireNonNull(due);
    }

    public LocalDateTime getDue() {
        return due;
    }

    public void setDue(LocalDateTime due) {
        this.due = Objects.requireNonNull(due);
    }

    // ✅ Dakika bazlı kontrol (yuvarlama hatası yok)
    public boolean isWithinHours(long hours) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(due)) return false;

        long minutesLeft = Duration.between(now, due).toMinutes();
        return minutesLeft <= hours * 60;
    }

    @Override
    public String toString() {
        return due.toString();
    }
}
