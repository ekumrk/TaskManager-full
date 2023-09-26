import managers.Managers;
import managers.TaskManager;
import tasks.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    public static final ZoneId zone = ZoneId.of("Europe/Simferopol");
    static TaskManager f;
    public static void main(String[] args) throws IOException {
        f = Managers.getDefaultHttpManager();
        Task task;
        Epic epic;
        Subtask subtask;


        //Тестирование
        // 1. создайте две задачи, эпик с тремя подзадачами и эпик без подзадач
        task = new Task("Почитать книгу", "Гарри Поттер",
                ZonedDateTime.of(LocalDateTime.parse("12:15 01.01.2023", DATE_TIME_FORMATTER), zone), 30);
        f.createNewTask(task);
        task = new Task("Посмотреть фильм", "Звёздные воины",
                ZonedDateTime.of(LocalDateTime.parse("11:15 02.01.2023", DATE_TIME_FORMATTER), zone), 120);
        f.createNewTask(task);

        epic = new Epic("Сходить в магазин", "Сделать покупки");
        f.createNewEpic(epic);
        subtask = new Subtask("Купить помидоры", "3 кг", 3,
                ZonedDateTime.of(LocalDateTime.parse("12:15 03.01.2023", DATE_TIME_FORMATTER), zone), 30);
        f.createNewSubtask(subtask);
        subtask = new Subtask("Купить яблоки", "2 кг", 3,
                ZonedDateTime.of(LocalDateTime.parse("12:15 04.01.2023", DATE_TIME_FORMATTER), zone), 30);
        f.createNewSubtask(subtask);
        subtask = new Subtask("Купить огурцы", "2 кг", 3,
                ZonedDateTime.of(LocalDateTime.parse("12:15 05.01.2023", DATE_TIME_FORMATTER), zone), 30);
        f.createNewSubtask(subtask);

        epic = new Epic("Сходить в культурные места", "Театры");
        f.createNewEpic(epic);

        f.getTaskFromId(2);
        f.getTaskFromId(1);
        f.getTaskFromId(2);
        System.out.println(f.getHistory());
        System.out.println(TaskManager.getPrioritySet());
    }
}
