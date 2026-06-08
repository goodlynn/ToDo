package com.example.lab2.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProjectTask implements TaskComponent {
    private final String title;
    private final List<TaskComponent> children = new ArrayList<>();

    public ProjectTask(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new TaskValidationException("Название проекта не может быть пустым.");
        }
        this.title = title.trim();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public double getProgress() {
        if (children.isEmpty()) {
            return 0;
        }
        return children.stream()
                .mapToDouble(TaskComponent::getProgress)
                .average()
                .orElse(0);
    }

    @Override
    public int getTotalTime() {
        return children.stream()
                .mapToInt(TaskComponent::getTotalTime)
                .sum();
    }

    @Override
    public Optional<LocalDate> getDeadline() {
        return children.stream()
                .map(TaskComponent::getDeadline)
                .flatMap(Optional::stream)
                .min(LocalDate::compareTo);
    }

    @Override
    public TaskStatus getStatus() {
        if (children.isEmpty() || children.stream().allMatch(child -> child.getStatus() == TaskStatus.NEW)) {
            return TaskStatus.NEW;
        }
        if (children.stream().allMatch(child -> child.getStatus() == TaskStatus.DONE)) {
            return TaskStatus.DONE;
        }
        return TaskStatus.IN_PROGRESS;
    }

    @Override
    public List<TaskComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void add(TaskComponent component) {
        if (component == null) {
            throw new TaskValidationException("Нельзя добавить пустой элемент.");
        }
        if (component == this) {
            throw new InvalidTaskOperationException("Проект нельзя добавить внутрь самого себя.");
        }
        if (children.contains(component)) {
            throw new InvalidTaskOperationException("Этот элемент уже есть в проекте.");
        }
        children.add(component);
    }

    @Override
    public void remove(TaskComponent component) {
        if (!children.remove(component)) {
            throw new InvalidTaskOperationException("Элемент не найден в проекте.");
        }
    }
}
