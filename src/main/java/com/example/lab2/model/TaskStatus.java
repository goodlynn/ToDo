package com.example.lab2.model;

public enum TaskStatus {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    DONE("Готово");

    private final String title;

    TaskStatus(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return this == DONE;
    }

    @Override
    public String toString() {
        return title;
    }
}
