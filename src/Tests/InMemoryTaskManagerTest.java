package Tests;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void init() throws IOException {
        super.init();
        manager = new InMemoryTaskManager();
    }
}
