package com.example.lab2.ui;

import com.example.lab2.model.ProjectTask;
import com.example.lab2.model.SimpleTask;
import com.example.lab2.model.TaskComponent;
import com.example.lab2.model.TaskException;
import com.example.lab2.model.TaskStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class TodoController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private ResourceBundle resources;

    @FXML
    private TreeView<TaskComponent> taskTree;

    @FXML
    private TextField titleField;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private Spinner<Integer> hoursSpinner;

    @FXML
    private ComboBox<TaskStatus> statusBox;

    @FXML
    private Label selectedTitleLabel;

    @FXML
    private Label selectedTypeLabel;

    @FXML
    private Label selectedDeadlineLabel;

    @FXML
    private Label selectedStatusLabel;

    @FXML
    private Label selectedTimeLabel;

    @FXML
    private Label selectedProgressLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label messageLabel;

    private ProjectTask rootProject;

    @FXML
    private void initialize() {
        statusBox.setItems(FXCollections.observableArrayList(TaskStatus.values()));
        statusBox.getSelectionModel().select(TaskStatus.NEW);
        deadlinePicker.setValue(LocalDate.now().plusDays(3));

        rootProject = new ProjectTask("Мои проекты");

        taskTree.setRoot(createTreeItem(rootProject));
        taskTree.setShowRoot(false);

        taskTree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(TaskComponent item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("overdue-cell", "done-cell");
                    return;
                }

                setText(formatTreeTitle(item));
                getStyleClass().removeAll("overdue-cell", "done-cell");

                if (item.isOverdue(LocalDate.now())) {
                    getStyleClass().add("overdue-cell");
                } else if (item.getStatus() == TaskStatus.DONE) {
                    getStyleClass().add("done-cell");
                }
            }
        });

        taskTree.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            showDetails(newItem == null ? rootProject : newItem.getValue());
        });

        refreshSummary();
    }

    @FXML
    private void addSimpleTask() {
        try {
            ProjectTask parent = selectedProject();

            SimpleTask task = new SimpleTask(
                    titleField.getText(),
                    deadlinePicker.getValue(),
                    hoursSpinner.getValue(),
                    statusBox.getValue()
            );

            parent.add(task);
            refreshTreeAndSelect(task);
            clearTitleField();
            showMessage(resources.getString("message.taskAdded"));
        } catch (TaskException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void addProject() {
        try {
            ProjectTask parent = selectedProject();

            ProjectTask project = new ProjectTask(titleField.getText());

            parent.add(project);
            refreshTreeAndSelect(project);
            clearTitleField();
            showMessage(resources.getString("message.projectAdded"));
        } catch (TaskException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void markDone() {
        TaskComponent selected = selectedComponent();

        if (selected instanceof SimpleTask simpleTask) {
            simpleTask.setStatus(TaskStatus.DONE);
            refreshTreeAndSelect(simpleTask);
            showMessage(resources.getString("message.done"));
        } else {
            showError(resources.getString("error.selectSimple"));
        }
    }

    @FXML
    private void removeSelected() {
        TreeItem<TaskComponent> selected = taskTree.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getParent() == null) {
            showError(resources.getString("error.removeRoot"));
            return;
        }

        try {
            TaskComponent parent = selected.getParent().getValue();
            parent.remove(selected.getValue());

            refreshTreeAndSelect(parent);
            showMessage(resources.getString("message.removed"));
        } catch (TaskException exception) {
            showError(exception.getMessage());
        }
    }

    private ProjectTask selectedProject() {
        TaskComponent selected = selectedComponent();

        if (selected instanceof ProjectTask projectTask) {
            return projectTask;
        }

        TreeItem<TaskComponent> item = taskTree.getSelectionModel().getSelectedItem();

        if (item != null && item.getParent() != null && item.getParent().getValue() instanceof ProjectTask projectTask) {
            return projectTask;
        }

        return rootProject;
    }

    private TaskComponent selectedComponent() {
        TreeItem<TaskComponent> item = taskTree.getSelectionModel().getSelectedItem();

        if (item == null) {
            return rootProject;
        }

        return item.getValue();
    }

    private void refreshTreeAndSelect(TaskComponent componentToSelect) {
        taskTree.setRoot(createTreeItem(rootProject));
        taskTree.setShowRoot(false);

        TreeItem<TaskComponent> itemToSelect = findItem(taskTree.getRoot(), componentToSelect);

        if (itemToSelect != null && itemToSelect != taskTree.getRoot()) {
            taskTree.getSelectionModel().select(itemToSelect);
        } else {
            taskTree.getSelectionModel().clearSelection();
        }

        taskTree.refresh();
        refreshSummary();
    }

    private TreeItem<TaskComponent> createTreeItem(TaskComponent component) {
        TreeItem<TaskComponent> item = new TreeItem<>(component);

        component.getChildren().stream()
                .map(this::createTreeItem)
                .forEach(item.getChildren()::add);

        item.setExpanded(true);
        return item;
    }

    private TreeItem<TaskComponent> findItem(TreeItem<TaskComponent> item, TaskComponent component) {
        if (item.getValue() == component) {
            return item;
        }

        return item.getChildren().stream()
                .map(child -> findItem(child, component))
                .filter(found -> found != null)
                .findFirst()
                .orElse(null);
    }

    private String formatTreeTitle(TaskComponent component) {
        String prefix = component instanceof ProjectTask ? "▣ " : "• ";
        return prefix + component.getTitle() + " - " + Math.round(component.getProgress()) + "%";
    }

    private void showDetails(TaskComponent component) {
        if (component == null) {
            component = rootProject;
        }

        selectedTitleLabel.setText(component.getTitle());

        selectedTypeLabel.setText(component instanceof ProjectTask
                ? resources.getString("type.project")
                : resources.getString("type.task"));

        selectedDeadlineLabel.setText(component.getDeadline()
                .map(deadline -> deadline.format(DATE_FORMAT))
                .orElse(resources.getString("value.noDeadline")));

        selectedStatusLabel.setText(component.getStatus().toString());
        selectedTimeLabel.setText(component.getTotalTime() + " ч");
        selectedProgressLabel.setText(Math.round(component.getProgress()) + "%");
        progressBar.setProgress(component.getProgress() / 100.0);
    }

    private void refreshSummary() {
        showDetails(selectedComponent());

        summaryLabel.setText(String.format(
                resources.getString("summary.format"),
                Math.round(rootProject.getProgress()),
                rootProject.getTotalTime(),
                rootProject.getChildren().size()
        ));
    }

    private void clearTitleField() {
        titleField.clear();
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().remove("error-message");
    }

    private void showError(String message) {
        messageLabel.setText(message);

        if (!messageLabel.getStyleClass().contains("error-message")) {
            messageLabel.getStyleClass().add("error-message");
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(resources.getString("dialog.errorTitle"));
        alert.setHeaderText(resources.getString("dialog.errorHeader"));
        alert.setContentText(message);
        alert.showAndWait();
    }
}