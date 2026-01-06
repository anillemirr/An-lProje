package com.ntp.taskmanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Proje ve görevlerin yönetiminden sorumlu servis sınıfıdır.
 */
public class ProjectManager {

    private final Map<String, Project> projects = new HashMap<>();
    private final Map<String, Task> tasks = new HashMap<>();

    private final ReminderService reminderService = new ReminderService();

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
    public Project getProjectByIdOrName(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) {
            throw new IllegalArgumentException("Project ID/Name boş olamaz.");
        }

        String input = idOrName.trim();

        // 1) Önce ID dene
        Project byId = projects.get(input);
        if (byId != null) return byId;

        // 2) Sonra isme göre ara (case-insensitive, trim)
        String target = input.toLowerCase();
        List<Project> matches = new ArrayList<>();

        for (Project p : projects.values()) {
            String name = p.getName();
            if (name != null && name.trim().toLowerCase().equals(target)) {
                matches.add(p);
            }
        }

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Project bulunamadı (ID/Name): " + input);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Birden fazla proje aynı isimde. Lütfen Project ID kullan.");
        }
        return matches.get(0);
    }

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
            if (t.getShortId().equalsIgnoreCase(idOrShort.trim())) {
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

    public void assignTaskToProject(String taskIdOrShortId, String projectIdOrName) {
        Task task = getTaskByIdOrShortId(taskIdOrShortId);
        Project project = getProjectByIdOrName(projectIdOrName);
        project.addTask(task);
    }

    public void completeTask(String taskIdOrShortId) {
        Task t = getTaskByIdOrShortId(taskIdOrShortId);
        t.complete();
    }

    public String deleteTask(String taskIdOrShortId) {
        Task t = getTaskByIdOrShortId(taskIdOrShortId);
        String fullId = t.getId();

        for (Project p : projects.values()) {
            p.removeTaskById(fullId);
        }

        tasks.remove(fullId);
        return fullId;
    }

    public void updateTask(String idOrShort,
                           String newTitle,
                           String newDesc,
                           Priority newPriority,
                           LocalDateTime newDeadline) {

        Task t = getTaskByIdOrShortId(idOrShort);

        if (newTitle != null && !newTitle.isBlank()) t.setTitle(newTitle.trim());
        if (newDesc != null) t.setDescription(newDesc);
        if (newPriority != null) t.setPriority(newPriority);
        if (newDeadline != null) t.getDeadline().setDue(newDeadline);
    }

    /* ===================== LISTING ===================== */

    public List<Task> listUpcomingTasks(String projectIdOrName, long withinHours) {
        Project project = getProjectByIdOrName(projectIdOrName);
        List<Task> result = new ArrayList<>();

        for (Task t : project.getTasks()) {
            if (t.isCompleted()) continue;
            if (t.getDeadline().isOverdue()) continue;
            if (t.getDeadline().isWithinHours(withinHours)) result.add(t);
        }

        result.sort(
                Comparator.comparing(Task::getPriority, Comparator.comparingInt(Priority::getLevel))
                        .reversed()
                        .thenComparing(x -> x.getDeadline().getDue())
        );
        return result;
    }

    public List<Task> listProjectTasks(String projectIdOrName, Boolean completedFilter) {
        Project project = getProjectByIdOrName(projectIdOrName);
        List<Task> result = new ArrayList<>();

        for (Task t : project.getTasks()) {
            if (completedFilter == null || t.isCompleted() == completedFilter) result.add(t);
        }

        result.sort(
                Comparator.comparing(Task::isCompleted)
                        .thenComparing(Comparator.comparing(Task::getPriority, Comparator.comparingInt(Priority::getLevel)).reversed())
                        .thenComparing(x -> x.getDeadline().getDue())
        );
        return result;
    }

    public List<Task> searchProjectTasks(String projectIdOrName,
                                         String keyword,
                                         boolean searchInDescription,
                                         Boolean completedFilter) {

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Arama kelimesi boş olamaz.");
        }

        Project project = getProjectByIdOrName(projectIdOrName);
        String k = keyword.trim().toLowerCase();

        List<Task> result = new ArrayList<>();

        for (Task t : project.getTasks()) {
            if (completedFilter != null && t.isCompleted() != completedFilter) continue;

            boolean hitTitle = t.getTitle() != null && t.getTitle().toLowerCase().contains(k);
            boolean hitDesc = false;

            if (searchInDescription) {
                String d = t.getDescription();
                hitDesc = d != null && d.toLowerCase().contains(k);
            }

            if (hitTitle || hitDesc) result.add(t);
        }

        result.sort(
                Comparator.comparing(Task::getPriority, Comparator.comparingInt(Priority::getLevel))
                        .reversed()
                        .thenComparing(x -> x.getDeadline().getDue())
        );

        return result;
    }

    public List<Task> searchProjectTasksAdvanced(String projectIdOrName,
                                                 String keyword,
                                                 boolean searchInDescription,
                                                 Boolean completedFilter,
                                                 Long onlyUpcomingWithinHours,
                                                 int limit) {

        List<Task> base = searchProjectTasks(projectIdOrName, keyword, searchInDescription, completedFilter);

        LocalDateTime now = LocalDateTime.now();
        List<Task> filtered = new ArrayList<>();

        for (Task t : base) {
            if (onlyUpcomingWithinHours == null) {
                filtered.add(t);
                continue;
            }

            if (t.isCompleted()) continue;
            if (t.getDeadline().isOverdue()) continue;

            long hoursLeft = Duration.between(now, t.getDeadline().getDue()).toHours();
            if (hoursLeft >= 0 && hoursLeft <= onlyUpcomingWithinHours) filtered.add(t);
        }

        filtered.sort(Comparator.comparing(x -> x.getDeadline().getDue()));

        if (limit > 0 && filtered.size() > limit) {
            return new ArrayList<>(filtered.subList(0, limit));
        }
        return filtered;
    }

    public List<Task> runReminders(String projectIdOrName, long withinMinutes) {
        Project project = getProjectByIdOrName(projectIdOrName);
        return reminderService.getTasksToRemind(project.getTasks(), withinMinutes);
    }

    /* ===================== CSV EXPORT / IMPORT ===================== */

    public String exportProjectAsCSV(String projectIdOrName) {
        Project project = getProjectByIdOrName(projectIdOrName);

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

    public Path exportProjectCSVToFile(String projectIdOrName, String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) throw new IllegalArgumentException("filePath boş olamaz.");

        String csv = exportProjectAsCSV(projectIdOrName);
        Path path = Path.of(filePath);

        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        Files.writeString(path, csv, StandardCharsets.UTF_8);
        return path;
    }

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

    public ImportResult importTasksFromCSV(String projectIdOrName, String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) throw new IllegalArgumentException("CSV dosya yolu boş olamaz.");

        Project project = getProjectByIdOrName(projectIdOrName);
        Path path = Path.of(filePath);

        if (!Files.exists(path)) throw new IllegalArgumentException("CSV dosyası bulunamadı: " + path.toAbsolutePath());

        Set<String> existingKeys = new HashSet<>();
        for (Task t : project.getTasks()) existingKeys.add(makeKey(t.getTitle(), t.getDeadline().getDue()));

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return new ImportResult(0, 0);

        int added = 0, skipped = 0;

        for (int i = 1; i < lines.size(); i++) {
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
