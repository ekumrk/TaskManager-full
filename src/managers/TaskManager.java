package managers;

import ProgrammExceptions.CrossTimeException;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.util.*;

public interface TaskManager {


    Map<Integer, Task> tasks = new HashMap<>();
    Map<Integer, Subtask> subtasks = new HashMap<>();
    Map<Integer, Epic> epics = new HashMap<>();

    Set<Task> prioritySet = new TreeSet<>((task1, task2) -> {
        if (task1.startTime == null) {
            return -1;
        }
        if (task1.startTime.isAfter(task2.startTime)) {
            return 1;
        }
        if (task1.startTime.isBefore(task2.startTime)) {
            return -1;
        }
        if (task1.startTime.equals(task2.startTime)) {
            return 0;
        }
        return 0;
    });

    public void createNewTask(Task task) throws IOException;

    public void updateTask(Task task);

    public List<Task> getTaskList();

    public void deleteAllTasks();

    public Task getTaskFromId(int id) throws IOException;

    public void deleteTaskFromId(int id);

    public void createNewEpic(Epic epic) throws IOException;

    public ArrayList<Epic> getEpicList();

    public Epic getEpicFromId(int id) throws IOException;

    public ArrayList<Subtask> getSubtasksFromEpicId (int id);

    public void deleteAllEpics();

    public void deleteEpicFromId(int id);

    public void createNewSubtask(Subtask subtask) throws IOException;

    public ArrayList<Subtask> getSubtaskList();

    public void deleteAllSubtasks();

    public void updateEpicStatus(Epic epic);

    public Subtask getSubtaskFromId(int id) throws IOException;
    public void deleteSubtaskFromId(int id);

    public void updateSubtask(Subtask subtask);

    public List<Task> getHistory();

    public static Set<Task> getPrioritySet() {
        return prioritySet;
    }

    public void ifCrosses(Task task) throws CrossTimeException;
    public void clearPriotitySet();
}
