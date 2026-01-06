package com.ntp.taskmanager;

/**
 * Proje bilgilerini ve kısa kullanım yönergelerini tutar.
 *
 * <p>
 
 */
public final class ProjectInfo {

    public static final String APP_NAME = "Görev & Proje Yönetim Aracı";
    public static final String VERSION = "1.0.0";

    private ProjectInfo() {
        // utility class
    }

    /**
     * Uygulama başlığını konsola yazdırır.
     */
    public static void printBanner() {
        System.out.println("====================================");
        System.out.println(APP_NAME + " v" + VERSION);
        System.out.println("OOP: Encapsulation | Inheritance | Interface");
        System.out.println("====================================");
    }

    /**
     * Kısa kullanım bilgisi verir.
     */
    public static void printQuickHelp() {
        System.out.println("\nKısa Kullanım:");
        System.out.println("1) Proje oluştur -> Project ID kopyala");
        System.out.println("3) Görev oluştur -> Task ID kopyala");
        System.out.println("4) Görevi projeye ata (Task ID + Project ID)");
        System.out.println("5) Görev tamamla (Task ID)");
        System.out.println("6) Yaklaşan görevleri listele (Project ID + saat)");
        System.out.println("7) CSV yazdır (Project ID)");
    }
}
