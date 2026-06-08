package com.example.lab2.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface TaskComponent {
    String getTitle();

    double getProgress();

    int getTotalTime();

    Optional<LocalDate> getDeadline();

    TaskStatus getStatus();

    default boolean isOverdue(LocalDate today) {
        return getDeadline()
                .map(deadline -> deadline.isBefore(today) && !getStatus().isDone())
                .orElse(false);
    }

    default List<TaskComponent> getChildren() {
        return Collections.emptyList();
    }

    default void add(TaskComponent component) {
        throw new InvalidTaskOperationException("В простую задачу нельзя добавить подзадачу.");
    }

    default void remove(TaskComponent component) {
        throw new InvalidTaskOperationException("У простой задачи нет вложенных элементов.");
    }
}
