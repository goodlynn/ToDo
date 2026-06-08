package com.example.lab2.model;

import java.time.LocalDate;
import java.util.Optional;

public class SimpleTask implements TaskComponent {
    private final String title;
    private final LocalDate deadline;
    private final int plannedHours;
    private TaskStatus status;

    public SimpleTask(String title, LocalDate deadline, int plannedHours, TaskStatus status) {
        if (title == null || title.trim().isEmpty()) {
            throw new TaskValidationException("Название задачи не может быть пустым.");
        }
        if (deadline == null) {
            throw new TaskValidationException("Для простой задачи нужен дедлайн.");
        }
        if (plannedHours <= 0) {
            throw new TaskValidationException("Плановое время должно быть больше нуля.");
        }
        if (status == null) {
            throw new TaskValidationException("Статус задачи не указан.");
        }
        this.title = title.trim();
        this.deadline = deadline;
        this.plannedHours = plannedHours;
        this.status = status;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public double getProgress() {
        return switch (status) {
            case NEW -> 0;
            case IN_PROGRESS -> 50;
            case DONE -> 100;
        };
    }

    @Override
    public int getTotalTime() {
        return plannedHours;
    }

    @Override
    public Optional<LocalDate> getDeadline() {
        return Optional.of(deadline);
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        if (status == null) {
            throw new TaskValidationException("Статус задачи не указан.");
        }
        this.status = status;
    }
}
