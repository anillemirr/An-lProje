package com.ntp.taskmanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Proje ve görevlerin yönetiminden sorumlu servis sınıfıdır.
 */
public class ProjectManager {
    private final Map<String, Project> projects = new HashMap<>();
    private final Map<String, Task> tasks = new HashMap<>();

    public Project createProject(String name) {
        Project p = new Project(name);
        projects.put(p.getId(), p);
        return p;
    }

    public Task createTask(String title, String desc, LocalDateTime due, Priority pr) {
        Task t = new Task(title, desc, new Deadline(due), pr);
        tasks.put(t.getId(), t);
        return t;
    }

    public TimedTask createTimedTask(String title, String desc, LocalDateTime due, Priority pr,
                                     LocalDateTime start, LocalDateTime end) {
        TimedTask t = new TimedTask(title, desc, new Deadline(due), pr, start, end);
        tasks.put(t.getId(), t);
        return t;
    }

    public Collection<Project> getAllProjects() {
        return Collections.unmodifiableCollection(projects.values());
    }

    public Project getProjectById(String projectId) {
        Project p = projects.get(projectId);
        if (p == null) throw new IllegalArgumentException("Project not found: " + projectId);
        return p;
    }

    public Task getTaskById(String taskId) {
        Task t = tasks.get(taskId);
        if (t == null) throw new IllegalArgumentException("Task not found: " + taskId);
        return t;
    }

    public void assignTaskToProject(String taskId, String projectId) {
        Task task = getTaskById(taskId);
        Project project = getProjectById(projectId);
        project.addTask(task);
    }

    public void completeTask(String taskId) {
        Task t = getTaskById(taskId);
        t.complete();
    }

    /**
     * Yaklaşan görevleri listeler:
     * - Tamamlananları dışarıda bırakır
     * - Süresi geçmiş (overdue) olanları dışarıda bırakır
     * - Önce öncelik (YUKSEK->DUSUK), sonra deadline (en yakın önce) sıralar
     */
    public List<Task> listUpcomingTasks(String projectId, long withinHours) {
        Project project = getProjectById(projectId);

        List<Task> result = new ArrayList<>();
        for (Task t : project.getTasks()) {
            if (t.isCompleted()) continue;
            if (t.getDeadline().isOverdue()) continue;
            if (t.getDeadline().isWithinHours(withinHours)) {
                result.add(t);
            }
        }

        result.sort(
                Comparator.comparing(Task::getPriority,
                                Comparator.comparingInt(Priority::getLevel))
                        .reversed()
                        .thenComparing(t -> t.getDeadline().getDue())
        );

        return result;
    }

    /**
     * Projeye ait tüm görevleri döndürür.
     * completedFilter:
     * - null  => tüm görevler
     * - true  => sadece tamamlananlar
     * - false => sadece tamamlanmayanlar
     */
    public List<Task> listProjectTasks(String projectId, Boolean completedFilter) {
        Project project = getProjectById(projectId);

        List<Task> result = new ArrayList<>();
        for (Task t : project.getTasks()) {
            if (completedFilter == null) {
                result.add(t);
            } else if (t.isCompleted() == completedFilter) {
                result.add(t);
            }
        }

        result.sort(
                Comparator.comparing(Task::isCompleted) // false önce
                        .thenComparing(
                                Comparator.comparing(Task::getPriority,
                                        Comparator.comparingInt(Priority::getLevel)).reversed()
                        )
                        .thenComparing(t -> t.getDeadline().getDue())
        );

        return result;
    }

    public String exportProjectAsCSV(String projectId) {
        Project project = getProjectById(projectId);

        StringBuilder sb = new StringBuilder("title,priority,deadline,completed\n");
        for (Task t : project.getTasks()) {
            sb.append(escape(t.getTitle())).append(",")
              .append(t.getPriority()).append(",")
              .append(t.getDeadline().getDue()).append(",")
              .append(t.isCompleted()).append("\n");
        }
        return sb.toString();
    }

    /**
     *
     * @param projectId proje id
     * @param filePath kaydedilecek dosya yolu (örn: C:\temp\project.csv)
     * @return kaydedilen dosyanın Path değeri
     */
    public Path exportProjectCSVToFile(String projectId, String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath boş olamaz.");
        }

        String csv = exportProjectAsCSV(projectId);
        Path path = Path.of(filePath);

        
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Files.writeString(path, csv, StandardCharsets.UTF_8);
        return path;
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
