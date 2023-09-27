package managers;

import java.io.IOException;

public class Managers {

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static TaskManager getDefault(String direction) throws IOException {
        return new FileBackedTaskManager(direction);
    }
}
