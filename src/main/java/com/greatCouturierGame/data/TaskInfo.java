package com.greatCouturierGame.data;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Task information class
 *
 * @author Roman
 */
@Entity(name = "TASK_INFO")
public class TaskInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    private String taskTypeId;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskTypeId() {
        return taskTypeId;
    }

    public void setTaskTypeId(String taskTypeId) {
        this.taskTypeId = taskTypeId;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
