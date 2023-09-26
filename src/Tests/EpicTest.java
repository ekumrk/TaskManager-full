package Tests;

import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    public static final ZoneId zone = ZoneId.of("Europe/Simferopol");

    private static TaskManager manager;
    private static Epic epic;

    @BeforeEach
    public void BeforeEach() throws IOException {
        manager = new InMemoryTaskManager();
        epic = new Epic("title", "content");
        manager.createNewEpic(epic);
    }

    @AfterEach
    public void AfterEach() {
        manager.tasks.clear();
        manager.subtasks.clear();
        manager.epics.clear();
        manager.clearPriotitySet();
    }

    @Test
    void shouldBeNewIfListOfSubtasksIsEmpty() {
        Status result = epic.getStatus();
        assertEquals(Status.NEW, result);
    }

    @Test
    void shouldBeNewIfAllSubtasksAreNew() throws IOException {
        Subtask subtask1 = new Subtask("subtask1Title", "subtask1Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        manager.createNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("subtask2Title","subtask2Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("13:00 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        manager.createNewSubtask(subtask2);
        Status result = epic.getStatus();
        assertEquals(Status.NEW, result);
    }

    @Test
    void shouldBeDoneWhenAllSubtasksDone() throws IOException {
        Subtask subtask1 = new Subtask("subtask1Title", "subtask1Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        manager.createNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("subtask2Title","subtask2Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("13:00 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        manager.createNewSubtask(subtask2);

        Subtask subtask1ForUpdate = new Subtask(2, "subtask1Title", "subtask1Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30, Status.DONE);
        Subtask subtask2ForUpdate = new Subtask(3, "subtask2Title","subtask2Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("13:00 03.01.2023", DATE_TIME_FORMATTER), zone), 30, Status.DONE);

        manager.updateSubtask(subtask1ForUpdate);
        manager.updateSubtask(subtask2ForUpdate);

        Status result = epic.getStatus();
        assertEquals(Status.DONE, result);
    }

    @Test
    void shouldBeInProgressIfSubtasksAreNewOrDone() throws IOException {
        Subtask subtask1 = new Subtask("subtask1Title", "subtask1Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        manager.createNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("subtask2Title","subtask2Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("13:00 03.01.2023", DATE_TIME_FORMATTER), zone), 30);

        manager.createNewSubtask(subtask2);
        Subtask subtask1ForUpdate = new Subtask(2, "subtask1Title", "subtask1Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30, Status.DONE);
        manager.updateSubtask(subtask1ForUpdate);
        Status result = epic.getStatus();
        assertEquals(Status.IN_PROGRESS, result);
    }

    @Test
    void shouldBeInProgressWhenAllSubtasksAreInProgress() throws IOException {
        Subtask subtask1 = new Subtask("subtask1Title", "subtask1Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        manager.createNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("subtask2Title","subtask2Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("13:00 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        manager.createNewSubtask(subtask2);

        Subtask subtask1ForUpdate = new Subtask(2, "subtask1Title", "subtask1Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30, Status.IN_PROGRESS);
        Subtask subtask2ForUpdate = new Subtask(3, "subtask2Title","subtask2Content", epic.getId(),
                ZonedDateTime.of(LocalDateTime.parse("13:00 03.01.2023", DATE_TIME_FORMATTER), zone), 30, Status.IN_PROGRESS);

        manager.updateSubtask(subtask1ForUpdate);
        manager.updateSubtask(subtask2ForUpdate);
        Status result = epic.getStatus();
        assertEquals(Status.IN_PROGRESS, result);
    }
}