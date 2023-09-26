package managers;

import ProgrammExceptions.CrossTimeException;
import ProgrammExceptions.ManagerSaveException;
import tasks.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    public static final ZoneId zone = ZoneId.of("Europe/Simferopol");
    private static final String HOME = System.getProperty("user.home");
    private final Path historyFile;

    public List<Task> allTasks = new ArrayList<>();

    public FileBackedTaskManager() {
        historyFile = null;
    }

    public FileBackedTaskManager(String direction) throws IOException {
        historyFile = Paths.get(HOME, direction);
        if (Files.exists(historyFile) && Files.size(historyFile) != 0) {
            readListFromFile();
        }
    }

    public void save() throws IOException {
        try {
        if (Files.notExists(historyFile)) {
            Files.createFile(historyFile);
        }
            List<String> allTasksToString = allTasks.stream()
                    .map(Task::toString)
                    .collect(Collectors.toList());
            String history = historyToString(historyManager);
            List<Integer> historyToInteger = historyFromString(history);
            writeToFile(allTasksToString, historyToInteger);
        } catch (ManagerSaveException e) {
                System.out.println("Ошибка сохранения в файл");
        }
    }

    @Override
    public void createNewTask(Task task) throws IOException {
        try {
            ifCrosses(task);
        } catch (CrossTimeException e) {
            System.out.println(e.getMessage());
            return;
        }
        super.createNewTask(task);
        allTasks.add(task);
        save();
    }

    @Override
    public void createNewEpic(Epic epic) throws IOException {
        super.createNewEpic(epic);
        allTasks.add(epic);
        save();
    }

    @Override
    public void createNewSubtask(Subtask subtask) throws IOException {
        try {
            ifCrosses(subtask);
        } catch (CrossTimeException e) {
            System.out.println(e.getMessage());
            return;
        }
        super.createNewSubtask(subtask);
        allTasks.add(subtask);
        save();
    }

    public void readListFromFile() {
           try {
           String fileContent = Files.readString(historyFile);
           String[] lines = fileContent.split("\n");

           for (int i = 1; i < lines.length; i++) {
               if (!lines[i].isBlank()) {
                   List<String> lineContent = List.of(lines[i].split(","));

                   if (lineContent.get(1).equals(TaskTypes.TASK.toString())) {
                       createNewTask(new Task(lineContent.get(2), lineContent.get(4),
                               ZonedDateTime.of(LocalDateTime.parse(lineContent.get(5), DATE_TIME_FORMATTER), zone),
                               Integer.parseInt(lineContent.get(6))));
                   } else if (lineContent.get(1).equals(TaskTypes.EPIC.toString())) {
                       createNewEpic(new Epic(lineContent.get(2), lineContent.get(4)));
                   } else if (lineContent.get(1).equals(TaskTypes.SUBTASK.toString())) {
                       int epicId = Integer.parseInt(lineContent.get(8));
                       createNewSubtask(new Subtask(lineContent.get(2), lineContent.get(4), epicId,
                               ZonedDateTime.of(LocalDateTime.parse(lineContent.get(5), DATE_TIME_FORMATTER), zone),
                               Integer.parseInt(lineContent.get(6))));
                   } else {
                       for (String o: lineContent) {
                           int id = Integer.parseInt(o);
                           if (tasks.containsKey(id)) {
                               getTaskFromId(id);
                           } else if (epics.containsKey(id)) {
                               getEpicFromId(id);
                           } else if (subtasks.containsKey(id)) {
                               getSubtaskFromId(id);
                           }
                       }
                   }
               }
           }
       } catch (IOException e) {
                System.out.println("Произошла ошибка во время чтения файла.");

       }
    }

    public void writeToFile(List<String> tasks, List<Integer> history) throws IOException {
        try (FileWriter writer = new FileWriter(String.valueOf(historyFile), StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,startTime,duration,endTime,epicID\n");
            for (String element : tasks) {
                writer.write(element + "\n");
            }
            writer.write("\n");

            if (history !=null ) {
                for (int i = 0; i < history.size(); i++) {
                    if (i == 0) {
                        writer.write(history.get(i) + ",");
                    } else {
                        writer.write(history.get(i) + "");
                    }
                }
            }
        }
    }

    static List<Integer> historyFromString(String value) {
        if (!value.isBlank()) {
            List<Integer> taskIds = new ArrayList<>();
            String[] values = value.split(",");
            for (String v : values) {
                if (!v.isBlank()) {
                    taskIds.add(Integer.parseInt(v.trim()));
                }
            }
            return taskIds;
        }
        return null;
    }

    static String historyToString(HistoryManager manager) {
        List<Task> tasksHistory = manager.getHistory();
        StringBuilder builder = new StringBuilder();
        for (Task t : tasksHistory) {
            builder.append(t.getId()).append(", ");
        }
        return builder.toString();
    }

    @Override
    public Task getTaskFromId(int id) throws IOException {
        super.getTaskFromId(id);
        save();
        return tasks.get(id);
    }

    @Override
    public Epic getEpicFromId(int id) throws IOException {
        super.getEpicFromId(id);
        save();
        return epics.get(id);
    }

    @Override
    public Subtask getSubtaskFromId(int id) throws IOException {
        super.getSubtaskFromId(id);
        save();
        return subtasks.get(id);
    }

    public Path getHistoryFile() {
        return historyFile;
    }

}
