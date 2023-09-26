package managers;
import ProgrammExceptions.CrossTimeException;
import tasks.*;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int nextId = 1;

    protected HistoryManager historyManager = Managers.getDefaultHistory();

    public InMemoryTaskManager() {
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void createNewTask(Task task) throws IOException {
        try {
            ifCrosses(task);
        } catch (CrossTimeException e) {
            System.out.println(e.getMessage());
            return;
        }
        prioritySet.add(task);
        task.setId(nextId);
        nextId++;
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        Task taskForDelete = tasks.get(id);
        prioritySet.remove(taskForDelete);

        tasks.put(id, task);
        prioritySet.add(task);
    }

    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task t : tasks.values()) {
            prioritySet.remove(t);
        }
        tasks.clear();
    }

    @Override
    public Task getTaskFromId(int id) throws IOException {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addToHistoryTask(task);
        }
        return tasks.get(id);
    }

    @Override
    public void deleteTaskFromId(int id) {
        historyManager.remove(id);
        Task task = tasks.get(id);
        prioritySet.remove(task);
        tasks.remove(id);
    }

    @Override
    public void createNewEpic(Epic epic) throws IOException {
        epic.setId(nextId);
        nextId++;
        epics.put(epic.getId(), epic);
    }

    @Override
    public ArrayList<Epic> getEpicList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicFromId(int id) throws IOException {
        Task task = (Task) epics.get(id);
        if (task != null) {
            historyManager.addToHistoryTask(task);
        }
        return epics.get(id);
    }

    @Override
    public ArrayList<Subtask> getSubtasksFromEpicId(int id) {
        ArrayList<Subtask> subtasksFromEpicId = new ArrayList<>();

        Epic epic = epics.get(id);
        for (Integer subtId : epic.getSubtasksId()) {
            Subtask subtask = subtasks.get(subtId);
            subtasksFromEpicId.add(subtask);
        }
        return subtasksFromEpicId;
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        for (Subtask s : subtasks.values()) {
            prioritySet.remove(s);
        }
        subtasks.clear();
    }

    @Override
    public void deleteEpicFromId(int id) {
        historyManager.remove(id);
        Epic epic = epics.remove(id);
        for (Integer subtaskId : epic.getSubtasksId()) {
            historyManager.remove(subtaskId);
            prioritySet.remove(subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
        }
    }

    @Override
    public void createNewSubtask(Subtask subtask) throws IOException {
        try {
            ifCrosses(subtask);
        } catch (CrossTimeException e) {
            System.out.println(e.getMessage());
            return;
        }
        prioritySet.add(subtask);
        subtask.setId(nextId);
        nextId++;
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        epic.getSubtasksId().add(subtask.getId());

        Comparator<ZonedDateTime> comparator = Comparator.comparing(
                zdt -> zdt.truncatedTo(ChronoUnit.MINUTES));
        TreeMap<ZonedDateTime, ZonedDateTime> subtasksStartAndEndTime = new TreeMap<>(comparator);
        List<Subtask> subtasksOfEpic = new ArrayList<>();

        for (Integer n : epic.getSubtasksId()) {
            subtasksOfEpic.add(subtasks.get(n));
        }

        for (Subtask subt : subtasksOfEpic) {
            subtasksStartAndEndTime.put(subt.getStartTime(), subt.getEndTime());
        }

        epic.setStartTime(subtasksStartAndEndTime.firstKey());
        ZonedDateTime lastKey = subtasksStartAndEndTime.lastKey();
        epic.setEndTime(subtasksStartAndEndTime.get(lastKey));

        Duration duration = Duration.between(epic.getStartTime(), epic.getEndTime());
        epic.setDuration((int) duration.toMinutes());


        updateEpicStatus(epic);
    }


    @Override
    public ArrayList<Subtask> getSubtaskList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasksId().clear();
            updateEpicStatus(epic);
        }

        for (Subtask s : subtasks.values()) {
            prioritySet.remove(s);
        }
        subtasks.clear();
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        ArrayList<Status> epicStatuses = new ArrayList<>();

        for (Integer subTaskId : epic.getSubtasksId()) {
            Subtask subTs = subtasks.get(subTaskId);
            if (subTs.getStatus().equals(Status.NEW)) {
                epicStatuses.add(Status.NEW);
            } else if (subTs.getStatus().equals(Status.IN_PROGRESS)) {
                epicStatuses.add(Status.IN_PROGRESS);
            } else if (subTs.getStatus().equals(Status.DONE)) {
                epicStatuses.add(Status.DONE);
            }
        }
        int counter = 0;
        if ((epicStatuses.contains(Status.IN_PROGRESS)) || (epicStatuses.contains(Status.NEW)
                && epicStatuses.contains(Status.IN_PROGRESS))
                || (epicStatuses.contains(Status.DONE) && epicStatuses.contains(Status.NEW))) {
            epic.setStatus(Status.IN_PROGRESS);
        }
        for (Status epicStatus : epicStatuses) {
            if (epicStatus.equals(Status.DONE)) {
                ++counter;
            }
        }
        if (counter == epicStatuses.size()) {
            epic.setStatus(Status.DONE);
        }
    }

    @Override
    public Subtask getSubtaskFromId(int id) throws IOException {
        Task task = subtasks.get(id);
        if (task != null) {
            historyManager.addToHistoryTask(task);
        }
        return subtasks.get(id);
    }

    @Override
    public void deleteSubtaskFromId(int id) {
        for (Epic epic : epics.values()) {
            if (epic.getSubtasksId().contains(id)) {
                epic.getSubtasksId().remove(id);
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
            Subtask subtask = subtasks.get(id);
            prioritySet.remove(subtask);
            subtasks.remove(id);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        Subtask subtaskForDelete = subtasks.get(id);
        prioritySet.remove(subtaskForDelete);

        subtasks.put(id, subtask);
        prioritySet.add(subtask);

        int epId = subtask.getEpicId();
        Epic epic = epics.get(epId);
        updateEpicStatus(epic);
    }

    @Override
    public void ifCrosses(Task task) throws CrossTimeException {
        for (Task t : prioritySet) {
            if (task.startTime.isBefore(t.getEndTime())) {
                throw new CrossTimeException("Введённая задача пересекается с другими, нельзя её добавить.");
            }
        }
    }

    @Override
    public void clearPriotitySet() {
        prioritySet.clear();
    }
}


