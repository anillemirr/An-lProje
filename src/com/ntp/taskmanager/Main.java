package com.ntp.taskmanager;

public class Main {
    public static void main(String[] args) {
        ProjectManager pm = new ProjectManager();
        ConsoleMenu menu = new ConsoleMenu(pm);
        menu.start();
    }
}
