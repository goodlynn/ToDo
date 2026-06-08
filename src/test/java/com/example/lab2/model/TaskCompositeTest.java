package com.example.lab2;

import com.example.lab2.model.InvalidTaskOperationException;
import com.example.lab2.model.ProjectTask;
import com.example.lab2.model.SimpleTask;
import com.example.lab2.model.TaskStatus;
import com.example.lab2.model.TaskValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskCompositeTest {
    @Test
    void projectAveragesChildProgressAndSumsTime() {
        ProjectTask project = new ProjectTask("Курсовой проект");
        project.add(new SimpleTask("План", LocalDate.now().plusDays(1), 2, TaskStatus.DONE));
        project.add(new SimpleTask("Код", LocalDate.now().plusDays(2), 6, TaskStatus.IN_PROGRESS));
        project.add(new SimpleTask("Отчет", LocalDate.now().plusDays(3), 1, TaskStatus.NEW));

        assertEquals(50, project.getProgress());
        assertEquals(9, project.getTotalTime());
        assertEquals(TaskStatus.IN_PROGRESS, project.getStatus());
    }

    @Test
    void projectReturnsNearestDeadlineFromChildren() {
        ProjectTask project = new ProjectTask("Сессия");
        LocalDate nearest = LocalDate.of(2026, 6, 10);
        project.add(new SimpleTask("Поздняя задача", LocalDate.of(2026, 6, 20), 2, TaskStatus.NEW));
        project.add(new SimpleTask("Срочная задача", nearest, 1, TaskStatus.NEW));

        assertEquals(nearest, project.getDeadline().orElseThrow());
    }

    @Test
    void simpleTaskRejectsChildren() {
        SimpleTask task = new SimpleTask("Одиночная задача", LocalDate.now().plusDays(1), 1, TaskStatus.NEW);

        assertThrows(InvalidTaskOperationException.class, () -> task.add(new ProjectTask("Подпроект")));
    }

    @Test
    void invalidTaskDataThrowsValidationException() {
        assertThrows(
                TaskValidationException.class,
                () -> new SimpleTask("", LocalDate.now().plusDays(1), 1, TaskStatus.NEW)
        );
        assertThrows(
                TaskValidationException.class,
                () -> new SimpleTask("Без времени", LocalDate.now().plusDays(1), 0, TaskStatus.NEW)
        );
    }

    @Test
    void unfinishedPastTaskIsOverdue() {
        SimpleTask task = new SimpleTask("Просроченная", LocalDate.of(2026, 6, 1), 1, TaskStatus.IN_PROGRESS);

        assertTrue(task.isOverdue(LocalDate.of(2026, 6, 9)));
    }
}
