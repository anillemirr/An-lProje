package com.ntp.taskmanager;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final ProjectManager pm;
    private final Scanner sc = new Scanner(System.in);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");

    public ConsoleMenu(ProjectManager pm) {
        this.pm = pm;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== GÖREV & PROJE YÖNETİMİ ===");
            System.out.println("1) Proje oluştur");
            System.out.println("2) Projeleri listele");
            System.out.println("3) Görev oluştur");
            System.out.println("4) Görevi projeye ata (ID / kısa ID)");
            System.out.println("5) Görev tamamla (ID / kısa ID)");
            System.out.println("6) Yaklaşan görevleri listele");
            System.out.println("7) Projeyi CSV olarak yazdır");
            System.out.println("8) Projedeki tüm görevleri listele");
            System.out.println("9) CSV'yi dosyaya kaydet");
            System.out.println("10) CSV'den görevleri yükle");
            System.out.println("11) Task detay görüntüle (ID / kısa ID)");
            System.out.println("12) Task sil (ID / kısa ID)");
            System.out.println("13) Task güncelle (ID / kısa ID)");
            System.out.println("14) Hatırlatmaları çalıştır");
            System.out.println("15) Projede görev ara"); 
            System.out.println("0) Çıkış");
            System.out.print("Seçim: ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createProject();
                    case "2" -> listProjects();
                    case "3" -> createTask();
                    case "4" -> assignTaskToProject();
                    case "5" -> completeTask();
                    case "6" -> listUpcoming();
                    case "7" -> exportCsv();
                    case "8" -> listAllProjectTasks();
                    case "9" -> exportCsvToFile();
                    case "10" -> importCsvFromFile();
                    case "11" -> showTaskDetails();
                    case "12" -> deleteTask();
                    case "13" -> updateTask();
                    case "14" -> runReminders();
                    case "15" -> searchTasksInProject();
                    case "0" -> {
                        System.out.println("Çıkış yapıldı.");
                        return;
                    }
                    default -> System.out.println("Geçersiz seçim.");
                }
            } catch (Exception e) {
                System.out.println("Hata: " + e.getMessage());
            }
        }
    }

    private void searchTasksInProject() {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        System.out.print("Aranacak kelime: ");
        String keyword = sc.nextLine().trim();

        System.out.print("Açıklamada da ara? (E/H): ");
        boolean inDesc = sc.nextLine().trim().equalsIgnoreCase("E");

        System.out.println("Filtre seç:");
        System.out.println("1) Tümü");
        System.out.println("2) Sadece tamamlanan");
        System.out.println("3) Sadece tamamlanmayan");
        System.out.print("Seçim: ");
        String f = sc.nextLine().trim();

        Boolean filter = null;
        if ("2".equals(f)) filter = true;
        else if ("3".equals(f)) filter = false;

        List<Task> result = pm.searchProjectTasks(projectId, keyword, inDesc, filter);
        if (result.isEmpty()) {
            System.out.println("Sonuç bulunamadı.");
            return;
        }

        System.out.println("--- ARAMA SONUÇLARI ---");
        for (Task t : result) {
            String status = t.isCompleted() ? "✅" : "🟡";
            System.out.println(status +
                    " | ID: " + t.getId() +
                    " | Kısa: " + t.getShortId() +
                    " | " + t.getTitle() +
                    " | Öncelik: " + t.getPriority().getLabel() +
                    " | Deadline: " + t.getDeadline().getDue());
        }
    }

    private void runReminders() {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        System.out.print("Kaç dakika içinde yaklaşanlar? (örn: 60): ");
        long mins = Long.parseLong(sc.nextLine().trim());

        List<Task> remind = pm.runReminders(projectId, mins);
        if (remind.isEmpty()) {
            System.out.println("Hatırlatma yok.");
            return;
        }

        System.out.println("--- HATIRLATMALAR ---");
        for (Task t : remind) {
            System.out.println("ID: " + t.getId() + " | Kısa: " + t.getShortId() + " | " + Notification.upcoming(t));
        }
    }

    private void updateTask() {
        System.out.print("Güncellenecek Task ID veya kısa ID: ");
        String idOrShort = sc.nextLine().trim();

        Task existing = pm.getTaskByIdOrShortId(idOrShort);
        System.out.println("Mevcut: " + existing);

        System.out.println("Boş bırakırsan aynı kalır.");

        System.out.print("Yeni başlık: ");
        String newTitle = sc.nextLine();

        System.out.print("Yeni açıklama (boş bırakabilirsin): ");
        String newDesc = sc.nextLine();

        System.out.print("Yeni öncelik (DUSUK/ORTA/YUKSEK) (boş=değişmesin): ");
        String pr = sc.nextLine().trim().toUpperCase();
        Priority newPriority = null;
        if (!pr.isBlank()) {
            newPriority = Priority.valueOf(pr);
        }

        System.out.print("Yeni deadline (yyyy-MM-dd H:mm) (boş=değişmesin): ");
        String dl = sc.nextLine().trim();
        LocalDateTime newDeadline = null;
        if (!dl.isBlank()) {
            newDeadline = LocalDateTime.parse(dl, fmt);
        }

        pm.updateTask(idOrShort,
                newTitle == null || newTitle.isBlank() ? null : newTitle,
                newDesc,
                newPriority,
                newDeadline);

        System.out.println("Task güncellendi.");
        System.out.println("Yeni: " + pm.getTaskByIdOrShortId(idOrShort));
    }

    private void createProject() {
        System.out.print("Proje adı: ");
        String name = sc.nextLine().trim();
        Project p = pm.createProject(name);
        System.out.println("Proje oluşturuldu. ID: " + p.getId());
    }

    private void listProjects() {
        var projects = pm.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("Proje yok.");
            return;
        }
        System.out.println("--- Projeler ---");
        for (Project p : projects) {
            System.out.println("ID: " + p.getId() + " | " + p.getName() + " | Görev: " + p.getTasks().size());
        }
    }

    private void createTask() {
        System.out.println("Görev tipi seç:");
        System.out.println("1) Normal Task");
        System.out.println("2) TimedTask (başlangıç-bitiş)");
        System.out.print("Seçim: ");
        String type = sc.nextLine().trim();

        System.out.print("Başlık: ");
        String title = sc.nextLine().trim();

        System.out.print("Açıklama: ");
        String desc = sc.nextLine().trim();

        Priority pr = readPriority();
        LocalDateTime due = readDateTime("Deadline (yyyy-MM-dd H:mm): ");

        if ("2".equals(type)) {
            LocalDateTime start = readDateTime("Start (yyyy-MM-dd H:mm): ");
            LocalDateTime end = readDateTime("End (yyyy-MM-dd H:mm): ");
            TimedTask t = pm.createTimedTask(title, desc, due, pr, start, end);
            System.out.println("TimedTask oluşturuldu.");
            System.out.println("ID: " + t.getId() + " | Kısa ID: " + t.getShortId());
        } else {
            Task t = pm.createTask(title, desc, due, pr);
            System.out.println("Task oluşturuldu.");
            System.out.println("ID: " + t.getId() + " | Kısa ID: " + t.getShortId());
        }
    }

    private void assignTaskToProject() {
        System.out.print("Task ID veya kısa ID: ");
        String taskIdOrShort = sc.nextLine().trim();

        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        pm.assignTaskToProject(taskIdOrShort, projectId);
        System.out.println("Görev projeye atandı.");
    }

    private void completeTask() {
        System.out.print("Tamamlanacak Task ID veya kısa ID: ");
        String taskIdOrShort = sc.nextLine().trim();

        pm.completeTask(taskIdOrShort);
        System.out.println("Görev tamamlandı.");
    }

    private void listUpcoming() {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        System.out.print("Kaç saat içinde? (örn: 24): ");
        long hours = Long.parseLong(sc.nextLine().trim());

        List<Task> upcoming = pm.listUpcomingTasks(projectId, hours);
        if (upcoming.isEmpty()) {
            System.out.println("Yaklaşan görev yok (" + hours + " saat içinde).");
            return;
        }

        System.out.println("--- Yaklaşan Görevler ---");
        for (Task t : upcoming) {
            System.out.println("ID: " + t.getId() + " | Kısa: " + t.getShortId() + " | " + Notification.upcoming(t));
        }
    }

    private void exportCsv() {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        String csv = pm.exportProjectAsCSV(projectId);
        System.out.println("\n--- CSV ---");
        System.out.println(csv);
    }

    private void listAllProjectTasks() {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        System.out.println("Filtre seç:");
        System.out.println("1) Tümü");
        System.out.println("2) Sadece tamamlanan");
        System.out.println("3) Sadece tamamlanmayan");
        System.out.print("Seçim: ");
        String f = sc.nextLine().trim();

        Boolean filter = null;
        if ("2".equals(f)) filter = true;
        else if ("3".equals(f)) filter = false;

        List<Task> list = pm.listProjectTasks(projectId, filter);
        if (list.isEmpty()) {
            System.out.println("Görev bulunamadı.");
            return;
        }

        System.out.println("--- Görevler ---");
        for (Task t : list) {
            String status = t.isCompleted() ? "✅ Tamamlandı" : "🟡 Devam ediyor";
            System.out.println(status +
                    " | ID: " + t.getId() +
                    " | Kısa: " + t.getShortId() +
                    " | " + t.getTitle() +
                    " | Öncelik: " + t.getPriority().getLabel() +
                    " | Deadline: " + t.getDeadline().getDue());
        }
    }

    private void exportCsvToFile() throws Exception {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        System.out.print("Dosya yolu (örn: C:\\temp\\project.csv): ");
        String path = sc.nextLine().trim();

        Path saved = pm.exportProjectCSVToFile(projectId, path);
        System.out.println("CSV kaydedildi: " + saved.toAbsolutePath());
    }

    private void importCsvFromFile() throws Exception {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        System.out.print("CSV dosya yolu: ");
        String path = sc.nextLine().trim();

        ProjectManager.ImportResult result = pm.importTasksFromCSV(projectId, path);
        System.out.println("İçe aktarma tamamlandı. Eklenen: " + result.getAdded() + " | Atlanan: " + result.getSkipped());
    }

    private void showTaskDetails() {
        System.out.print("Task ID veya kısa ID: ");
        String idOrShort = sc.nextLine().trim();

        Task t = pm.getTaskByIdOrShortId(idOrShort);

        System.out.println("\n--- TASK DETAY ---");
        System.out.println("ID: " + t.getId());
        System.out.println("Kısa ID: " + t.getShortId());
        System.out.println("Başlık: " + t.getTitle());
        System.out.println("Açıklama: " + (t.getDescription() == null ? "" : t.getDescription()));
        System.out.println("Öncelik: " + t.getPriority().getLabel());
        System.out.println("Deadline: " + t.getDeadline().getDue());
        System.out.println("Durum: " + (t.isCompleted() ? "✅ Tamamlandı" : "🟡 Devam ediyor"));

        if (t instanceof TimedTask tt) {
            System.out.println("Tür: TimedTask");
            System.out.println("Start: " + tt.getStart());
            System.out.println("End: " + tt.getEnd());
        } else {
            System.out.println("Tür: Task");
        }
        System.out.println("--------------");
    }

    private void deleteTask() {
        System.out.print("Silinecek Task ID veya kısa ID: ");
        String idOrShort = sc.nextLine().trim();

        System.out.print("Emin misin? (E/H): ");
        String confirm = sc.nextLine().trim().toUpperCase();

        if (!"E".equals(confirm)) {
            System.out.println("İptal edildi.");
            return;
        }

        String deletedId = pm.deleteTask(idOrShort);
        System.out.println("Task silindi. ID: " + deletedId);
    }

    private Priority readPriority() {
        while (true) {
            System.out.print("Öncelik (DUSUK/ORTA/YUKSEK): ");
            String p = sc.nextLine().trim().toUpperCase();
            try {
                return Priority.valueOf(p);
            } catch (Exception e) {
                System.out.println("Geçersiz öncelik.");
            }
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return LocalDateTime.parse(input, fmt);
            } catch (Exception e) {
                System.out.println("Format yanlış. Örnek: 2025-12-18 8:30 veya 2025-12-18 08:30");
            }
        }
    }
}
