package web;
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

import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;


public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static Gson gson;
    private TaskManager manager;
    private HttpServer httpServer;

    public HttpTaskServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler());
        manager = Managers.getDefault("/dev/java-sprint8-hw/resources/history.csv");
        gson = new GsonBuilder().serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new TimeAdapter())
                .create();
    }

    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            String query = exchange.getRequestURI().getQuery();
            Endpoint endpoint = getEndpoint(pathParts, exchange.getRequestMethod(), query);

            String id;
            InputStream inputStream;
            String body;

            switch (endpoint) {
                case GET_PRIORITITIZEDSET:
                    writeResponse(exchange, gson.toJson(TaskManager.getPrioritySet()), 200);
                    break;
                case GET_HISTORY:
                    writeResponse(exchange, gson.toJson(manager.getHistory()), 200);
                    break;
                case GET_SUBTASK_FROM_EPICID:
                    id = query.substring(3);
                    writeResponse(exchange, gson.toJson(manager.getSubtasksFromEpicId(Integer.parseInt(id))), 200);
                    break;
                case GET_TASKS:
                    writeResponse(exchange, gson.toJson(manager.getTaskList()), 200);
                    break;
                case GET_TASK_BY_ID:
                    id = query.substring(3);
                    writeResponse(exchange, gson.toJson(manager.getTaskFromId(Integer.parseInt(id))), 200);
                    break;
                case GET_SUBTASKS:
                    writeResponse(exchange, gson.toJson(manager.getSubtaskList()), 200);
                    break;
                case GET_SUBTASK_BY_ID:
                    id = query.substring(3);
                    writeResponse(exchange, gson.toJson(manager.getSubtaskFromId(Integer.parseInt(id))), 200);
                    break;
                case GET_EPICS:
                    writeResponse(exchange, gson.toJson(manager.getEpicList()), 200);
                    break;
                case GET_EPIC_BY_ID:
                    id = query.substring(3);
                    writeResponse(exchange, gson.toJson(manager.getEpicFromId(Integer.parseInt(id))), 200);
                    break;
                case POST_NEW_OR_UPDATE_TASK:
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
                case POST_NEW_OR_UPDATE_SUBTASK:
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
                case POST_NEW_EPIC:
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
                case DELETE_ALL_TASKS:
                    manager.deleteAllTasks();
                    writeResponse(exchange, "Все задачи удалены.", 200);
                    break;
                case DELETE_TASK_BY_ID:
                    id = query.substring(3);
                    manager.deleteTaskFromId(Integer.parseInt(id));
                    writeResponse(exchange, "Задача с id + " + id + "удалена.", 200);
                    break;
                case DELETE_ALL_SUBTASKS:
                    manager.deleteAllSubtasks();
                    writeResponse(exchange, "Все подзадачи удалены.", 200);
                    break;
                case DELETE_SUBTASK_BY_ID:
                    id = query.substring(3);
                    manager.deleteSubtaskFromId(Integer.parseInt(id));
                    writeResponse(exchange, "Подзадача с id + " + id + "удалена.", 200);
                    break;
                case DELETE_ALL_EPICS:
                    manager.deleteAllEpics();
                    writeResponse(exchange, "Все эпики удалены.", 200);
                    break;
                case DELETE_EPIC_BY_ID:
                    id = query.substring(3);
                    manager.deleteEpicFromId(Integer.parseInt(id));
                    writeResponse(exchange, "Подзадача с id + " + id + "удалена.", 200);
                    break;
                default:
                    writeResponse(exchange, "Некорректный запрос, попробуйте ещё раз.", 404);
            }
        }
    }

    private static Endpoint getEndpoint(String[] pathParts, String requestMethod, String query) {

        switch(requestMethod) {
            case "GET":
                if (pathParts[2].isBlank()) {
                    return Endpoint.GET_PRIORITITIZEDSET;
                } else if (pathParts[2].equals("history")) {
                    return Endpoint.GET_HISTORY;
                } else if (pathParts[4].startsWith("?id=")) {
                    return Endpoint.GET_SUBTASK_FROM_EPICID;
                } else {
                    if (pathParts[2].equals("task")) {
                       if (pathParts[3].isBlank()) {
                           return Endpoint.GET_TASKS;
                       } else if (query.startsWith("id=")) {
                           return Endpoint.GET_TASK_BY_ID;
                       }
                   } else if (pathParts[2].equals("subtask")) {
                       if (pathParts[3].isBlank()) {
                           return Endpoint.GET_SUBTASKS;
                       } else if (query.startsWith("id=")) {
                           return Endpoint.GET_SUBTASK_BY_ID;
                       }
                   } else if (pathParts[2].equals("epic")) {
                        if (pathParts[3].isBlank()) {
                            return Endpoint.GET_EPICS;
                        } else if (query.startsWith("id=")) {
                            return Endpoint.GET_EPIC_BY_ID;
                        }
                   }
                }
                break;
            case "POST":
                if (pathParts[2].equals("task")) {
                    return Endpoint.POST_NEW_OR_UPDATE_TASK;
                } else if (pathParts[2].equals("subtask")) {
                    return Endpoint.POST_NEW_OR_UPDATE_SUBTASK;
                } else if (pathParts[2].equals("epic")) {
                    return Endpoint.POST_NEW_EPIC;
                }
                break;
            case "DELETE":
                if (pathParts[2].equals("task")) {
                    if (pathParts[3].isBlank()) {
                        return Endpoint.DELETE_ALL_TASKS;
                    } else {
                        return Endpoint.DELETE_TASK_BY_ID;
                    }
                } else if (pathParts[2].equals("subtask")) {
                    if (pathParts[3].isBlank()) {
                        return Endpoint.DELETE_ALL_SUBTASKS;
                    } else {
                        return Endpoint.DELETE_SUBTASK_BY_ID;
                    }
                } else if (pathParts[2].equals("epic")) {
                    if (pathParts[3].isBlank()) {
                        return Endpoint.DELETE_ALL_EPICS;
                    } else {
                        return Endpoint.DELETE_EPIC_BY_ID;
                    }
                }
                break;
        }
        return Endpoint.UNKNOWN;
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

    enum Endpoint {GET_PRIORITITIZEDSET, GET_HISTORY, DELETE_EPIC_BY_ID, GET_SUBTASK_FROM_EPICID, GET_TASKS,
        GET_TASK_BY_ID, GET_SUBTASKS, GET_SUBTASK_BY_ID, GET_EPICS, GET_EPIC_BY_ID, POST_NEW_OR_UPDATE_TASK,
        POST_NEW_OR_UPDATE_SUBTASK, POST_NEW_EPIC, DELETE_ALL_TASKS, DELETE_TASK_BY_ID, DELETE_ALL_SUBTASKS,
        DELETE_SUBTASK_BY_ID, DELETE_ALL_EPICS, UNKNOWN}

    public void stop() {
        System.out.println("Остановлен сервер на порту " + PORT);
        httpServer.stop(0);
    }
}
