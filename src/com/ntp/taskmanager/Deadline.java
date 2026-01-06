package com.ntp.taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Görevlerin teslim tarihini temsil eden value object sınıfıdır.
 *
 * <p>
 * Deadline bilgisi kapsülleme (encapsulation) prensibine uygun olarak private tutulur.
 * Erişim kontrollü getter/setter ile sağlanır.
 * </p>
 */
public class Deadline {

    private LocalDateTime due; // encapsulated

    public Deadline(LocalDateTime due) {
        setDue(due);
    }

    public LocalDateTime getDue() {
        return due;
    }

    /**
     * Deadline günceller.
     * Null olamaz ve geçmiş tarih olarak ayarlanamaz.
     */
    public void setDue(LocalDateTime due) {
        Objects.requireNonNull(due, "Deadline (due) null olamaz.");
        // if (due.isBefore(LocalDateTime.now())) {
        //     throw new IllegalArgumentException("Deadline geçmiş bir tarih olamaz.");
        // }
        this.due = due;
    }

    /**
     * Deadline'ın belirtilen saat aralığı içinde olup olmadığını döndürür.
     * <p>Not: Dakika bazlı kontrol kullanılır (yuvarlama hatası yok).</p>
     *
     * @return true ise deadline belirtilen saat aralığı içindedir
     */
    public boolean isWithinHours(long hours) {
        if (hours < 0) throw new IllegalArgumentException("hours negatif olamaz.");

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(due)) return false;

        long minutesLeft = Duration.between(now, due).toMinutes();
        return minutesLeft <= hours * 60;
    }

    
    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(due);
    }

    @Override
    public String toString() {
        return due.toString();
    }
}
