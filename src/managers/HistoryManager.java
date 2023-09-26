package managers;

import tasks.Task;

import java.util.List;

public interface HistoryManager {
    List<Task> getHistory();
    void addToHistoryTask(Task task);
    void remove(int id);
}
