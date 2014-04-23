package com.aplana.timesheet.form;

/**
 * @author dsysterov
 * @version 1.0
 */
public class AdminProjectTaskForm {
    private Integer id;
    private String name;
    private String description;
    private Boolean active;
    private String priority;
    private String toDelete;

    public AdminProjectTaskForm() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getToDelete() {
        return toDelete;
    }

    public void setToDelete(String toDelete) {
        this.toDelete = toDelete;
    }
}
