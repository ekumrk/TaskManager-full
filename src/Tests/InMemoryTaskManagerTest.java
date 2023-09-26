package Tests;

import managers.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void init() throws IOException {
        super.init();
        manager = new InMemoryTaskManager();
    }

    @AfterEach
    void AfterEach() {
        manager.clearPriotitySet();
        manager.tasks.clear();
        manager.subtasks.clear();
        manager.epics.clear();
    }
}
