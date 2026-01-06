package com.ntp.taskmanager;

/**
 * Görevlerin öncelik seviyesini temsil eden enum.
 *
 * <p>
 * Öncelik seviyesi, görevlerin sıralanması ve
 * aciliyet durumlarının belirlenmesi için kullanılır.
 * </p>
 */
public enum Priority {

    DUSUK(1, "Düşük"),
    ORTA(2, "Orta"),
    YUKSEK(3, "Yüksek");

    private final int level;
    private final String label;

    Priority(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }
}
