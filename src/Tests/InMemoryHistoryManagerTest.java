package Tests;

import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryHistoryManagerTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    public static final ZoneId zone = ZoneId.of("Europe/Simferopol");
    private TaskManager manager;
    Task task;
    Task task2;
    Task task3;
    Task task4;
    Task task5;

    @BeforeEach
    void init() {
        manager = new InMemoryTaskManager();
        task = new Task("taskTitle", "taskContent",
                ZonedDateTime.of(LocalDateTime.parse("12:15 01.01.2023", DATE_TIME_FORMATTER), zone), 30);
        task2 = new Task("task2Title", "task2Content",
                ZonedDateTime.of(LocalDateTime.parse("13:15 01.01.2023", DATE_TIME_FORMATTER), zone), 30);
        task3 = new Task("task3Title", "task3Content",
                ZonedDateTime.of(LocalDateTime.parse("14:15 01.01.2023", DATE_TIME_FORMATTER), zone), 30);
        task4 = new Task("task4Title", "task4Content",
                ZonedDateTime.of(LocalDateTime.parse("15:15 01.01.2023", DATE_TIME_FORMATTER), zone), 30);
        task5 = new Task("task5Title", "task5Content",
                ZonedDateTime.of(LocalDateTime.parse("16:15 01.01.2023", DATE_TIME_FORMATTER), zone), 30);
    }

    @Test
    void getHistoryIfEmpty() {
        List<Task> result = manager.getHistory();
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistoryIfDoubled() throws IOException {
        manager.createNewTask(task);
        manager.createNewTask(task2);

        manager.getTaskFromId(task.getId());
        manager.getTaskFromId(task2.getId());
        manager.getTaskFromId(task.getId());

        List<Task> result = manager.getHistory();
        assertEquals(2, result.size());
    }

    @Test
    void getHistoryIfRemovedFromStartMiddleAndEnd() throws IOException {
        manager.createNewTask(task);
        manager.createNewTask(task2);
        manager.createNewTask(task3);
        manager.createNewTask(task4);
        manager.createNewTask(task5);

        manager.getTaskFromId(1);
        manager.getTaskFromId(2);
        manager.getTaskFromId(3);
        manager.getTaskFromId(4);
        manager.getTaskFromId(5);

        manager.deleteTaskFromId(1);
        manager.deleteTaskFromId(3);
        manager.deleteTaskFromId(5);

        List<Task> expected = List.of(task2,task4);
        List<Task> result = manager.getHistory();
        assertEquals(expected, result);

    }

}
