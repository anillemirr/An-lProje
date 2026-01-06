package com.ntp.taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Yaklaşan görevler için bildirim metni üretir.
 */
public class Notification {

    public static String upcoming(Task task) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = task.getDeadline().getDue();

        long minutes = Duration.between(now, due).toMinutes();
        if (minutes < 0) minutes = 0;

        long hoursPart = minutes / 60;
        long minutesPart = minutes % 60;

        return "⏰ Yaklaşan görev: " + task.getTitle()
                + " | Deadline: " + due
                + " | Öncelik: " + task.getPriority().getLabel()
                + " | Kalan: " + hoursPart + "s " + minutesPart + "dk";
    }
}
