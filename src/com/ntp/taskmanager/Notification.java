package com.ntp.taskmanager;

public class Notification {
    public static String upcoming(Task task) {
        return "⏰ Yaklaşan görev: " + task.getTitle()
                + " | Deadline: " + task.getDeadline().getDue()
                + " | Öncelik: " + task.getPriority();
    }
}
