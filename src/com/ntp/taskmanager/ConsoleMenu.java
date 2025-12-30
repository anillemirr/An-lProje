package com.ntp.taskmanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {
    private final ProjectManager pm;
    private final Scanner sc = new Scanner(System.in);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ConsoleMenu(ProjectManager pm) {
        this.pm = pm;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== GÖREV & PROJE YÖNETİMİ ===");
            System.out.println("1) Proje oluştur");
            System.out.println("2) Projeleri listele");
            System.out.println("3) Görev oluştur");
            System.out.println("4) Görevi projeye ata");
            System.out.println("5) Görev tamamla");
            System.out.println("6) Yaklaşan görevleri listele");
            System.out.println("7) Projeyi CSV olarak yazdır");
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

        LocalDateTime due = readDateTime("Deadline (yyyy-MM-dd HH:mm): ");

        if ("2".equals(type)) {
            LocalDateTime start = readDateTime("Start (yyyy-MM-dd HH:mm): ");
            LocalDateTime end = readDateTime("End (yyyy-MM-dd HH:mm): ");
            TimedTask t = pm.createTimedTask(title, desc, due, pr, start, end);
            System.out.println("TimedTask oluşturuldu. ID: " + t.getId());
        } else {
            Task t = pm.createTask(title, desc, due, pr);
            System.out.println("Task oluşturuldu. ID: " + t.getId());
        }
    }

    private void assignTaskToProject() {
        System.out.print("Task ID: ");
        String taskId = sc.nextLine().trim();

        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();

        pm.assignTaskToProject(taskId, projectId);
        System.out.println("Görev projeye atandı.");
    }

    private void completeTask() {
        System.out.print("Tamamlanacak Task ID: ");
        String taskId = sc.nextLine().trim();
        pm.completeTask(taskId);
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
            System.out.println(Notification.upcoming(t));
        }
    }

    private void exportCsv() {
        System.out.print("Project ID: ");
        String projectId = sc.nextLine().trim();
        String csv = pm.exportProjectAsCSV(projectId);
        System.out.println("\n--- CSV ---");
        System.out.println(csv);
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
                System.out.println("Format yanlış. Örnek: 2025-12-18 08:30");
            }
        }
    }
}
