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

    /* ===================== PROJECT & TASK ===================== */

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

    /** Tam ID ile. */
    public Task getTaskById(String taskId) {
        Task t = tasks.get(taskId);
        if (t == null) throw new IllegalArgumentException("Task not found: " + taskId);
        return t;
    }

    
    public Task getTaskByIdOrShortId(String idOrShort) {
        if (idOrShort == null || idOrShort.isBlank()) {
            throw new IllegalArgumentException("Task ID boş olamaz.");
        }

        Task direct = tasks.get(idOrShort);
        if (direct != null) return direct;

        List<Task> matches = new ArrayList<>();
        for (Task t : tasks.values()) {
            if (t.getShortId().equalsIgnoreCase(idOrShort)) {
                matches.add(t);
            }
        }

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Task bulunamadı: " + idOrShort);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Kısa ID birden fazla task ile eşleşti. Tam ID kullan.");
        }
        return matches.get(0);
    }

    public void assignTaskToProject(String taskIdOrShortId, String projectId) {
        Task task = getTaskByIdOrShortId(taskIdOrShortId);
        Project project = getProjectById(projectId);
        project.addTask(task);
    }

    public void completeTask(String taskIdOrShortId) {
        Task t = getTaskByIdOrShortId(taskIdOrShortId);
        t.complete();
    }

    /**
     * - Global task havuzundan siler
     * - Tüm projelerden kaldırır
     *
     * @return silinen task'ın tam ID'si (log/ekrana basmak için)
     */
    public String deleteTask(String taskIdOrShortId) {
        Task t = getTaskByIdOrShortId(taskIdOrShortId);
        String fullId = t.getId();

        // tüm projelerden kaldırır
        for (Project p : projects.values()) {
            p.removeTaskById(fullId);
        }

        // global havuzdan kaldırır
        tasks.remove(fullId);

        return fullId;
    }

    /* ===================== LISTING ===================== */

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
                        .thenComparing(x -> x.getDeadline().getDue())
        );

        return result;
    }

    public List<Task> listProjectTasks(String projectId, Boolean completedFilter) {
        Project project = getProjectById(projectId);
        List<Task> result = new ArrayList<>();

        for (Task t : project.getTasks()) {
            if (completedFilter == null || t.isCompleted() == completedFilter) {
                result.add(t);
            }
        }

        result.sort(
                Comparator.comparing(Task::isCompleted)
                        .thenComparing(
                                Comparator.comparing(Task::getPriority,
                                        Comparator.comparingInt(Priority::getLevel)).reversed()
                        )
                        .thenComparing(x -> x.getDeadline().getDue())
        );

        return result;
    }

    /* ===================== CSV EXPORT / IMPORT ===================== */

    public String exportProjectAsCSV(String projectId) {
        Project project = getProjectById(projectId);

        StringBuilder sb = new StringBuilder();
        sb.append("title,priority,deadline,completed\n");

        for (Task t : project.getTasks()) {
            sb.append(CsvUtil.escape(t.getTitle())).append(",")
              .append(CsvUtil.escape(t.getPriority().name())).append(",")
              .append(CsvUtil.escape(t.getDeadline().getDue().toString())).append(",")
              .append(CsvUtil.escape(Boolean.toString(t.isCompleted())))
              .append("\n");
        }

        return sb.toString();
    }

    public Path exportProjectCSVToFile(String projectId, String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath boş olamaz.");
        }

        String csv = exportProjectAsCSV(projectId);
        Path path = Path.of(filePath);

        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        Files.writeString(path, csv, StandardCharsets.UTF_8);
        return path;
    }

    /** Import sonucu: kaç eklendi / kaç atlandı. */
    public static class ImportResult {
        private final int added;
        private final int skipped;

        public ImportResult(int added, int skipped) {
            this.added = added;
            this.skipped = skipped;
        }

        public int getAdded() { return added; }
        public int getSkipped() { return skipped; }
    }

    /**
     * CSV dosyasından görevleri okuyup belirtilen projeye ekler.
     * Duplicate kuralı: aynı projede aynı title + aynı deadline varsa eklemez.
     */
    public ImportResult importTasksFromCSV(String projectId, String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("CSV dosya yolu boş olamaz.");
        }

        Project project = getProjectById(projectId);
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("CSV dosyası bulunamadı: " + path.toAbsolutePath());
        }

        Set<String> existingKeys = new HashSet<>();
        for (Task t : project.getTasks()) {
            existingKeys.add(makeKey(t.getTitle(), t.getDeadline().getDue()));
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return new ImportResult(0, 0);

        int added = 0;
        int skipped = 0;

        for (int i = 1; i < lines.size(); i++) { // header atla
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            List<String> parts = CsvUtil.parseLine(line);
            if (parts.size() < 4) continue;

            String title = parts.get(0);
            Priority priority = Priority.valueOf(parts.get(1));
            LocalDateTime deadline = LocalDateTime.parse(parts.get(2));
            boolean completed = Boolean.parseBoolean(parts.get(3));

            String key = makeKey(title, deadline);
            if (existingKeys.contains(key)) {
                skipped++;
                continue;
            }

            Task task = new Task(title, "", new Deadline(deadline), priority);
            if (completed) task.complete();

            tasks.put(task.getId(), task);
            project.addTask(task);

            existingKeys.add(key);
            added++;
        }

        return new ImportResult(added, skipped);
    }

    private String makeKey(String title, LocalDateTime deadline) {
        String t = (title == null) ? "" : title.trim().toLowerCase();
        return t + "||" + deadline.toString();
    }
}
