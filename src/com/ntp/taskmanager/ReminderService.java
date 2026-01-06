package com.ntp.taskmanager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basit hatırlatma servisi.
 *
 * - Yaklaşan görevleri kontrol eder.
 * - Belirlenen dakika aralığında deadline'a girenler için bildirim üretir.
 * - Aynı görev için aynı aralıkta tekrar bildirim basmaz.
 */
public class ReminderService {

    // "taskId|windowMinutes" gibi bir anahtarla tekrarları engelliyoruz
    private final Set<String> fired = new HashSet<>();

    /**
     * @param tasks kontrol edilecek görevler
     * @param withinMinutes kaç dakika içinde yaklaşanlar
     * @return bildirilecek görevlerin listesi
     */
    public List<Task> getTasksToRemind(List<Task> tasks, long withinMinutes) {
        LocalDateTime now = LocalDateTime.now();

        return tasks.stream()
                .filter(t -> !t.isCompleted())
                .filter(t -> !t.getDeadline().isOverdue())
                .filter(t -> {
                    long minutes = java.time.Duration.between(now, t.getDeadline().getDue()).toMinutes();
                    return minutes >= 0 && minutes <= withinMinutes;
                })
                .filter(t -> {
                    String key = t.getId() + "|" + withinMinutes;
                    if (fired.contains(key)) return false;
                    fired.add(key);
                    return true;
                })
                .toList();
    }

    public void reset() {
        fired.clear();
    }
}
