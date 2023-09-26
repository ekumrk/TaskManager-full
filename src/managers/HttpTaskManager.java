package managers;

import HttpServer.KVTaskClient;
import HttpServer.TimeAdapter;
import ProgrammExceptions.LoadException;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.time.LocalDateTime;

public class HttpTaskManager extends FileBackedTaskManager {
    private static KVTaskClient client;
    private static final Gson gson = new GsonBuilder().serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new TimeAdapter())
            .create();
    private static final String[] KEYS = {"TASKS", "EPICS", "SUBTASKS", "HISTORY"};
    public HttpTaskManager(String url) {
        client = new KVTaskClient(url);;
    }

    @Override
    public void save() {
        client.saveToServer(KEYS[0], gson.toJson(getTaskList()));
        client.saveToServer(KEYS[1], gson.toJson(getEpicList()));
        client.saveToServer(KEYS[2], gson.toJson(getSubtaskList()));
        client.saveToServer(KEYS[3], gson.toJson(getHistory()));
    }

    public static HttpTaskManager loadFromServer(String URL) throws IOException {
        HttpTaskManager manager = new HttpTaskManager(URL);

        for (String key : KEYS) {
            String json = client.load(key);
            if (json == null || json.equals("")) {
                continue;
            }

            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonArray()) {
                throw new LoadException("Ошибка при загрузке данных из клиента");
            }

            JsonArray array = element.getAsJsonArray();
            if (array.isEmpty()) {
                continue;
            }

            for (JsonElement e : array) {
                if (!e.isJsonObject()) {
                    throw new LoadException("Ошибка при загрузке данных из клиента");
                }

                switch (key) {
                    case "TASKS":
                        Task task = gson.fromJson(e, Task.class);
                        super.createNewTask(task);
                        break;
                    case "EPICS":
                        Epic epic = gson.fromJson(e, Epic.class);
                        super.createNewEpic(epic);
                        break;
                    case "SUBTASKS":
                        Subtask subtask = gson.fromJson(e, Subtask.class);
                        super.createNewSubtask(subtask);
                        break;
                    case "HISTORY":
                        int id = e.getAsJsonObject().get("id").getAsInt();
                        Task task1 = super.allTasks.get(id);
                        if (task1 != null) {
                            super.historyManager.addToHistoryTask(task1);
                        }
                    default:
                        throw new LoadException("Ошибка, неправильный тип задач при загрузке");
                }
            }
        }
        return manager;
    }
}

