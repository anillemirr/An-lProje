package com.ntp.taskmanager;

import java.time.LocalDateTime;
import java.util.*;

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

    // ✅ Menünün ihtiyacı: projeleri gör
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

    // ✅ ID ile atama (menü kolaylığı)
    public void assignTaskToProject(String taskId, String projectId) {
        Task task = getTaskById(taskId);
        Project project = getProjectById(projectId);
        project.addTask(task);
    }

    public void completeTask(String taskId) {
        Task t = getTaskById(taskId);
        t.complete();
    }

    // ✅ ID ile yaklaşan listeleme
    public List<Task> listUpcomingTasks(String projectId, long withinHours) {
        Project project = getProjectById(projectId);

        List<Task> result = new ArrayList<>();
        for (Task t : project.getTasks()) {
            if (!t.isCompleted() && t.getDeadline().isWithinHours(withinHours)) {
                result.add(t);
            }
        }
        result.sort(Comparator.comparing(Task::getPriority).reversed());
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

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
