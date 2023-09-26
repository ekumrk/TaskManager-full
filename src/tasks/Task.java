package tasks;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    protected int id;
    protected String title;
    protected String content;
    protected Status status;
    protected int duration;
    public ZonedDateTime startTime;

    public Task (String title, String content, ZonedDateTime startTime, int duration) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.duration = duration;
        this.status = Status.NEW;
    }

    public Task (int id, String title, String content, ZonedDateTime startTime, int duration, Status status) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.duration = duration;
        setStatus(status);
    }

    protected Task() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ZonedDateTime getEndTime() {
        return startTime.plusMinutes(duration);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }


    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", id, TaskTypes.TASK, title, status, content,
                startTime.format(DATE_TIME_FORMATTER), duration, getEndTime().format(DATE_TIME_FORMATTER));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(content, task.content) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, status);
    }
}
