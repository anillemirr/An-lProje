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

    public Task getTaskByIdOrShortId(String idOrShort) {
        if (idOrShort == null || idOrShort.isBlank()) {
            throw new IllegalArgumentException("Task ID boş olamaz.");
        }

        // Önce tam ID dene
        Task direct = tasks.get(idOrShort);
        if (direct != null) return direct;

        // Kısa ID ile ara
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
            throw new IllegalArgumentException("Kısa ID birden fazla task ile eşleşti.");
        }
        return matches.get(0);
    }

    public Task getTaskById(String taskId) {
        Task t = tasks.get(taskId);
        if (t == null) throw new IllegalArgumentException("Task not found: " + taskId);
        return t;
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
                        .thenComparing(t -> t.getDeadline().getDue())
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
                        .thenComparing(t -> t.getDeadline().getDue())
        );
        return result;
    }

    /* ===================== CSV EXPORT / IMPORT ===================== */

    public String exportProjectAsCSV(String projectId) {
        Project project = getProjectById(projectId);
        StringBuilder sb = new StringBuilder("title,priority,deadline,completed\n");

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
        String csv = exportProjectAsCSV(projectId);
        Path path = Path.of(filePath);

        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        Files.writeString(path, csv, StandardCharsets.UTF_8);
        return path;
    }

    public ImportResult importTasksFromCSV(String projectId, String filePath) throws IOException {
        Project project = getProjectById(projectId);
        Path path = Path.of(filePath);

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int added = 0, skipped = 0;

        Set<String> keys = new HashSet<>();
        for (Task t : project.getTasks()) {
            keys.add(t.getTitle().toLowerCase() + "||" + t.getDeadline().getDue());
        }

        for (int i = 1; i < lines.size(); i++) {
            List<String> parts = CsvUtil.parseLine(lines.get(i));
            if (parts.size() < 4) continue;

            String title = parts.get(0);
            Priority pr = Priority.valueOf(parts.get(1));
            LocalDateTime dl = LocalDateTime.parse(parts.get(2));
            boolean completed = Boolean.parseBoolean(parts.get(3));

            String key = title.toLowerCase() + "||" + dl;
            if (keys.contains(key)) {
                skipped++;
                continue;
            }

            Task t = new Task(title, "", new Deadline(dl), pr);
            if (completed) t.complete();

            tasks.put(t.getId(), t);
            project.addTask(t);
            keys.add(key);
            added++;
        }
        return new ImportResult(added, skipped);
    }

    /* ===================== IMPORT RESULT ===================== */

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
}
