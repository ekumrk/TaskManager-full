package Tests;

import manager.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasks.Task;

import java.nio.file.Files;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    Path historyPath;
    String historyFile = "historyFileTest.csv";

    @BeforeEach
    void init() throws IOException {
        try {
            historyPath = tempDir.resolve(historyFile);
        } catch(InvalidPathException ipe) {
            System.err.println(
                    "error creating temporary test file in " +
                            this.getClass().getSimpleName());
        }
        manager = new FileBackedTaskManager(historyFile);
        super.init();
    }

    @AfterEach
    void afterEach() throws IOException {
        Files.delete(manager.getHistoryFile());
    }

    @Test
    void correctSaveToFileIfNormalBehaviour() throws IOException {
        manager.createNewTask(task);
        assertTrue(Files.size(manager.getHistoryFile()) != 0);
    }

    @Test
    void correctReadFromFile() throws IOException {
        manager.createNewTask(task);
        manager.tasks.clear();
        manager.allTasks.clear();
        manager.clearPriotitySet();
        manager.readListFromFile();
        assertFalse(manager.tasks.isEmpty());
    }

    @Override
    public void ifCorrectGettingHistory() {
        if (Files.exists(manager.getHistoryFile())) {
            super.ifCorrectGettingHistory();
        }
    }

    @Override
    void deleteTaskFromId() {
        if (Files.exists(manager.getHistoryFile())) {
            super.deleteTaskFromId();
        }
    }

    @Override
    void getEpicListIfEpicsAreNotCreated() {
        if (Files.exists(manager.getHistoryFile())) {
            super.getEpicListIfEpicsAreNotCreated();
        }
    }

    @Override
    public void deleteAllEpicsInAnyCase() {
        if (Files.exists(manager.getHistoryFile())) {
            super.deleteAllEpicsInAnyCase();
        }
    }

    @Override
    public void getSubtasksFromEpicIdIfEpicsEmpty() {
        if (Files.exists(manager.getHistoryFile())) {
            super.getSubtasksFromEpicIdIfEpicsEmpty();
        }
    }

    @Override
    public void deleteAllTasksIfTaskListIsNotEmpty() {
        if (Files.exists(manager.getHistoryFile())) {
            super.deleteAllTasksIfTaskListIsNotEmpty();
        }
    }

    @Override
    void getTaskListIfStandardBehaviour() {
        if (Files.exists(manager.getHistoryFile())) {
            super.getTaskListIfStandardBehaviour();
        }
    }

    @Override
    void getSubtaskIfEpicsAreNotCreated() {
        if (Files.exists(manager.getHistoryFile())) {
            super.getSubtaskIfEpicsAreNotCreated();
        }
    }

    @Override
    public void deleteAllSubtasksIfSubtaskListIsNotEmpty() {
        if (Files.exists(manager.getHistoryFile())) {
            super.deleteAllSubtasksIfSubtaskListIsNotEmpty();
        }
    }

    @Override
    void getSubtaskListIfStandardBehaviour() {
        if (Files.exists(manager.getHistoryFile())) {
            super.getSubtaskListIfStandardBehaviour();
        }
    }

    @Override
    void getEpicListIfStandardBehaviour() {
        if (Files.exists(manager.getHistoryFile())) {
            super.getEpicListIfStandardBehaviour();
        }
    }

    @Override
    void deleteEpicFromIdInAnyCase() {
        if (Files.exists(manager.getHistoryFile())) {
            super.deleteEpicFromIdInAnyCase();
        }
    }

    @Override
    void deleteSubtaskFromIdInAnyCase() {
        if (Files.exists(manager.getHistoryFile())) {
            super.deleteSubtaskFromIdInAnyCase();
        }
    }

    @Override
    void getTaskListIfTasksAreNotCreated() {
        if (Files.exists(manager.getHistoryFile())) {
            super.getTaskListIfTasksAreNotCreated();
        }
    }

    @Test
    void saveAndReadFromFileIfAllTaskAreNotCreated() throws IOException {
        manager.allTasks.clear();
        manager.save();
        String fileContent = Files.readString(manager.getHistoryFile());
        String[] fileContents = fileContent.split("\n");
        assertTrue(fileContents.length == 1); //только заголовок с id, type итд

        manager.readListFromFile();
        assertTrue(manager.allTasks.isEmpty());
    }

    @Test
    void saveAndReadFromFileIfEpicWithoutSubtasks() throws IOException {
        manager.createNewEpic(epic);
        String fileContent = Files.readString(manager.getHistoryFile());
        String[] fileContents = fileContent.split("\n");
        String result = fileContents[1];
        assertEquals(epic.toString(), result);

        manager.allTasks.clear();
        manager.epics.clear();

        manager.readListFromFile();
        assertFalse(manager.epics.isEmpty());
    }

    @Test
    void saveAndReadFromFileIfHistoryIsEmpty() throws IOException {
        manager.createNewTask(task);
        manager.save();

        List<Task> result = manager.getHistory();
        assertTrue(result.isEmpty());

        manager.allTasks.clear();
        manager.tasks.clear();

        manager.readListFromFile();
        List<Task> result2 = manager.getHistory();
        assertTrue(result2.isEmpty());
    }
}
