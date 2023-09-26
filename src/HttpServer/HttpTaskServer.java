package HttpServer;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import managers.HttpTaskManager;
import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;


public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static Gson gson;
    private static TaskManager manager;
    private static HttpServer httpServer;

    public HttpTaskServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler());
        manager = Managers.getDefault("/dev/java-sprint8-hw/resources/history.csv");
        gson = new GsonBuilder().serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new TimeAdapter())
                .create();
    }

    static class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");

            String endpoint = getEndpoint(pathParts, exchange.getRequestMethod());

            String id;
            InputStream inputStream;
            String body;

            switch (endpoint) {
                case "Get_PriorititizedSet":
                    writeResponse(exchange, gson.toJson(TaskManager.getPrioritySet()), 200);
                    break;
                case "Get_History":
                    writeResponse(exchange, gson.toJson(manager.getHistory()), 200);
                    break;
                case "Get_SubtasksByEpicId":
                    id = pathParts[4].substring(4);
                    writeResponse(exchange, gson.toJson(manager.getSubtasksFromEpicId(Integer.parseInt(id))), 200);
                    break;
                case "Get_Tasks":
                    writeResponse(exchange, gson.toJson(manager.getTaskList()), 200);
                    break;
                case "Get_TaskById":
                    id = pathParts[3].substring(4);
                    writeResponse(exchange, gson.toJson(manager.getTaskFromId(Integer.parseInt(id))), 200);
                    break;
                case "Get_Subtasks":
                    writeResponse(exchange, gson.toJson(manager.getSubtaskList()), 200);
                    break;
                case "Get_SubtaskById":
                    id = pathParts[3].substring(4);
                    writeResponse(exchange, gson.toJson(manager.getSubtaskFromId(Integer.parseInt(id))), 200);
                    break;
                case "Get_Epics":
                    writeResponse(exchange, gson.toJson(manager.getEpicList()), 200);
                    break;
                case "Get_EpicById":
                    //TODO
                    id = exchange.getRequestURI().getQuery();
                    id = pathParts[3].substring(4);
                    writeResponse(exchange, gson.toJson(manager.getEpicFromId(Integer.parseInt(id))), 200);
                    break;
                case "Post_NewOrUpdateTask":
                    inputStream = exchange.getRequestBody();
                    body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                    try {
                        Task task = gson.fromJson(body, Task.class);
                        if (manager.tasks.containsKey(task.getId())) {
                            manager.updateTask(task);
                            writeResponse(exchange, "Задача обновлена", 201);
                        } else {
                            manager.createNewTask(task);
                            writeResponse(exchange, "Задача добавлена", 201);
                        }
                    } catch (JsonSyntaxException e) {
                        exchange.sendResponseHeaders(400, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Получен некорректный JSON".getBytes());
                        }
                    }
                    break;
                case "Post_NewOpUpdateSubtask":
                    inputStream = exchange.getRequestBody();
                    body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                    try {
                        Subtask subtask = gson.fromJson(body, Subtask.class);
                        if (manager.subtasks.containsKey(subtask.getId())) {
                            manager.updateSubtask(subtask);
                            writeResponse(exchange, "Подзадача обновлена", 201);
                        } else {
                            manager.createNewSubtask(subtask);
                            writeResponse(exchange, "Подзадача добавлена", 201);
                        }
                    } catch (JsonSyntaxException e) {
                        exchange.sendResponseHeaders(400, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Получен некорректный JSON".getBytes());
                        }
                    }
                    break;
                case "Post_NewEpic":
                    inputStream = exchange.getRequestBody();
                    body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                    try {
                        Epic epic = gson.fromJson(body, Epic.class);
                        manager.createNewEpic(epic);
                        writeResponse(exchange, "Эпик добавлен!",200);
                    } catch (JsonSyntaxException e) {
                        exchange.sendResponseHeaders(400, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Получен некорректный JSON".getBytes());
                        }
                    }
                    break;
                case "Delete_AllTasks":
                    manager.deleteAllTasks();
                    writeResponse(exchange, "Все задачи удалены.", 200);
                    break;
                case "Delete_TaskById":
                    id = pathParts[3].substring(4);
                    manager.deleteTaskFromId(Integer.parseInt(id));
                    writeResponse(exchange, "Задача с id + " + id + "удалена.", 200);
                    break;
                case "Delete_AllSubtasks":
                    manager.deleteAllSubtasks();
                    writeResponse(exchange, "Все подзадачи удалены.", 200);
                    break;
                case "Delete_SubtaskById":
                    id = pathParts[3].substring(4);
                    manager.deleteSubtaskFromId(Integer.parseInt(id));
                    writeResponse(exchange, "Подзадача с id + " + id + "удалена.", 200);
                    break;
                case "Delete_All_Epics":
                    manager.deleteAllEpics();
                    writeResponse(exchange, "Все эпики удалены.", 200);
                    break;
                case "Delete_Epic_ById":
                    id = pathParts[3].substring(4);
                    manager.deleteEpicFromId(Integer.parseInt(id));
                    writeResponse(exchange, "Подзадача с id + " + id + "удалена.", 200);
                    break;
                default:
                    writeResponse(exchange, "Некорректный запрос, попробуйте ещё раз.", 404);
            }
        }
    }

    private static String getEndpoint(String[] pathParts, String requestMethod) {

        switch(requestMethod) {
            case "GET":
                if (pathParts[2].isBlank()) {
                    return "Get_PriorititizedSet";
                } else if (pathParts[2].equals("history")) {
                    return "Get_History";
                } else if (pathParts[4].startsWith("?id=")) {
                    return "Get_SubtasksByEpicId";
                } else {
                    if (pathParts[2].equals("task")) {
                       if (pathParts[3].isBlank()) {
                           return "Get_Tasks";
                       } else if (pathParts[3].startsWith("?id=")) {
                           return "Get_TaskById";
                       }
                   } else if (pathParts[2].equals("subtask")) {
                       if (pathParts[3].isBlank()) {
                           return "Get_Subtasks";
                       } else if (pathParts[3].startsWith("?id=")) {
                           return "Get_SubtaskById";
                       }
                   } else if (pathParts[2].equals("epic")) {
                        if (pathParts[3].isBlank()) {
                            return "Get_Epics";
                        } else if (pathParts[3].startsWith("?id=")) {
                            return "Get_EpicById";
                        }
                   }
                }
                break;
            case "POST":
                if (pathParts[2].equals("task")) {
                    return "Post_NewOrUpdateTask";
                } else if (pathParts[2].equals("subtask")) {
                    return "Post_NewOpUpdateSubtask";
                } else if (pathParts[2].equals("epic")) {
                    return "Post_NewEpic";
                }
                break;
            case "DELETE":
                if (pathParts[2].equals("task")) {
                    if (pathParts[3].isBlank()) {
                        return "Delete_AllTasks";
                    } else {
                        return "Delete_TaskById";
                    }
                } else if (pathParts[2].equals("subtask")) {
                    if (pathParts[3].isBlank()) {
                        return "Delete_AllSubtasks";
                    } else {
                        return "Delete_SubtaskById";
                    }
                } else if (pathParts[2].equals("epic")) {
                    if (pathParts[3].isBlank()) {
                        return "Delete_All_Epics";
                    } else {
                        return "Delete_Epic_ById";
                    }
                }
                break;
        }
        return "UNKNOWN";
    }
    private static void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        if(responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    public void stop() {
        System.out.println("Остановлен сервер на порту " + PORT);
        httpServer.stop(0);
    }
}
