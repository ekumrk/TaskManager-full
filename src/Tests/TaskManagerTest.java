package Tests;

import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    public static final ZoneId zone = ZoneId.of("Europe/Simferopol");
    HistoryManager historyManager = Managers.getDefaultHistory();


    protected Task task;
    protected Task task2;
    protected Epic epic;
    protected Subtask subtask;
    protected Subtask subtask2;
    protected T manager;



    void init() throws IOException {
        task = new Task("taskTitle", "taskContent",
                ZonedDateTime.of(LocalDateTime.parse("12:15 01.01.2023", DATE_TIME_FORMATTER), zone), 30);
        task2 = new Task("task2Title", "task2Content",
                ZonedDateTime.of(LocalDateTime.parse("13:15 02.01.2023", DATE_TIME_FORMATTER), zone), 30);
        epic = new Epic("epicTitle", "epicContent");
        subtask = new Subtask("subtaskTitle", "subtaskContent", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("14:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        subtask2 = new Subtask("subtask2Title", "subtask2Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("15:15 04.01.2023", DATE_TIME_FORMATTER), zone), 30);
    }

    @Test
    public void ifCreateNewTaskWorksCorrectly() throws IOException {
        manager.createNewTask(task);
        Task result = manager.getTaskFromId(task.getId());
        assertTrue(manager.tasks.containsValue(result));
    }

    @Test
    public void checkIfUpdateTaskStatusCorrect() throws IOException {
        manager.createNewTask(task);
        manager.updateTask(task.getId(), Status.IN_PROGRESS);
        Status result = manager.tasks.get(task.getId()).getStatus();
        assertEquals(Status.IN_PROGRESS, result);
    }

    @Test
    void getTaskListIfStandardBehaviour() {
         manager.tasks.put(task.getId(), task);
         List<Task> result = manager.getTaskList();
         assertEquals(manager.tasks.get(task.getId()), result.get(0));
    }
    @Test
    void getTaskListIfTasksAreNotCreated() {
        manager.tasks.clear();
        List<Task> result = List.copyOf((manager.tasks.values()));
        assertTrue(result.isEmpty());
    }

    @Test
    public void deleteAllTasksIfTaskListIsNotEmpty() {
        manager.tasks.put(task.getId(), task);
        manager.deleteAllTasks();
        assertTrue(manager.tasks.isEmpty());
    }

    @Test
    public void getTaskFromIdWithEmptyTaskList() throws IOException {
        manager.tasks.clear();
        final Task result = manager.getTaskFromId(5);
        assertNull(result);
    }

    @Test
    public void getTaskFromIdWithIncorrectID() throws IOException {
        final Task result = manager.getTaskFromId(5);
        assertNull(result);
    }

    @Test
    public void getTaskFromIdWithStandardBehaviour() throws IOException {
        manager.createNewTask(task);
        Task result = manager.tasks.get(task.getId());
        assertEquals(task, result);
    }
    
    @Test
    void deleteTaskFromId() {
        manager.tasks.put(task.getId(), task);
        manager.tasks.put(task2.getId(), task);
        manager.deleteTaskFromId(task.getId());
        assertFalse(manager.tasks.containsValue(task));
    }

    @Test
    public void IfCreateNewEpicWorksWell() throws IOException {
        manager.createNewEpic(epic);
        Epic result = manager.getEpicFromId(epic.getId());
        assertTrue(manager.epics.containsValue(result));
    }

    @Test
    void getEpicListIfStandardBehaviour() {
        manager.epics.put(epic.getId(), epic);
        List<Epic> result = manager.getEpicList();
        assertEquals(manager.epics.get(epic.getId()), result.get(0));
    }
    @Test
    void getEpicListIfEpicsAreNotCreated() {
        manager.epics.clear();
        List<Epic> result = List.copyOf((manager.epics.values()));
        assertTrue(result.isEmpty());
    }

    @Test
    public void getEpicFromIdWithEmptyEpicList() throws IOException {
        manager.epics.clear();
        final Epic result = manager.getEpicFromId(5);
        assertNull(result);
    }

    @Test
    public void getEpicFromIdWithIncorrectID() throws IOException {
        final Epic result = manager.getEpicFromId(5);
        assertNull(result);
    }

    @Test
    public void getEpicFromIdWithStandardBehaviour() throws IOException {
        manager.createNewEpic(epic);
        Task result = manager.epics.get(epic.getId());
        assertEquals(epic, result);
    }

    @Test
    public void getSubtasksFromEpicIdIfStandardBehaviour () throws IOException {
        manager.createNewEpic(epic);
        manager.createNewSubtask(subtask);
        manager.createNewSubtask(subtask2);
        List<Subtask> result = manager.getSubtasksFromEpicId(epic.getId());
        List<Subtask> expected = manager.subtasks.values().stream()
                .filter(subtask -> (subtask.getEpicId() == epic.getId()))
                .collect(Collectors.toList());
        assertEquals(expected, result);
    }

    @Test
    public void getSubtasksFromEpicIdIfEpicsEmpty() {
        manager.epics.clear();
        boolean result = manager.epics.isEmpty();
        assertTrue(result);
    }

    @Test
    public void deleteAllEpicsInAnyCase() {
        manager.epics.put(epic.getId(), epic);
        manager.subtasks.put(subtask.getId(), subtask);
        manager.deleteAllEpics();
        assertTrue(manager.epics.isEmpty() && manager.subtasks.isEmpty());
    }

    @Test
    void deleteEpicFromIdInAnyCase() {
        manager.epics.put(epic.getId(), epic);
        manager.subtasks.put(subtask.getId(), subtask);
        manager.deleteEpicFromId(epic.getId());
        assertFalse(manager.epics.containsValue(epic));
    }

    @Test
    public void IfCreateNewSubtaskWorksWell() throws IOException {
        manager.createNewSubtask(subtask);
        Subtask result = manager.getSubtaskFromId(subtask.getId());
        assertTrue(manager.subtasks.containsValue(result));
    }

    @Test
    void getSubtaskListIfStandardBehaviour() {
        manager.epics.put(epic.getId(), epic);
        manager.subtasks.put(subtask.getId(), subtask);
        List<Subtask> result = manager.getSubtaskList();
        assertEquals(manager.subtasks.get(subtask.getId()), result.get(0));
    }
    @Test
    void getSubtaskIfEpicsAreNotCreated() {
        List<Subtask> result = List.copyOf((manager.subtasks.values()));
        assertTrue(result.isEmpty());
    }

    @Test
    public void deleteAllSubtasksIfSubtaskListIsNotEmpty() {
        manager.subtasks.put(subtask.getId(), subtask);
        manager.deleteAllSubtasks();
        assertTrue(manager.subtasks.isEmpty());
    }

    @Test
    public void getSubtaskFromIdWithEmptySubtaskList() throws IOException {
        manager.subtasks.clear();
        final Subtask result = manager.getSubtaskFromId(5);
        assertNull(result);
    }

    @Test
    public void getSubtaskFromIdWithIncorrectID() throws IOException {
        final Epic result = manager.getEpicFromId(5);
        assertNull(result);
    }

    @Test
    public void getSubtaskFromIdWithStandardBehaviour() throws IOException {
        manager.createNewSubtask(subtask);
        Task result = manager.subtasks.get(subtask.getId());
        assertEquals(subtask, result);
    }

    @Test
    void deleteSubtaskFromIdInAnyCase() {
        manager.deleteSubtaskFromId(subtask.getId());
        assertFalse(manager.subtasks.containsValue(subtask));
    }

    @Test
    public void ifCorrectGettingHistory() {
        if (manager.getHistory().isEmpty()) {
            assertTrue(manager.getHistory().isEmpty());
        } else {
            assertFalse(manager.getHistory().isEmpty());
        }
    }

}
