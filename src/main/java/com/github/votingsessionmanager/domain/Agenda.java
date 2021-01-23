package com.github.votingsessionmanager.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Agenda {

    @Id
    private String id;

    private String description;
    private String result;
    private AgendaStatus status;

    public Agenda(){}

    public Agenda(String id, String description, AgendaStatus status) {
        this.id = id;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public AgendaStatus getStatus() {
        return status;
    }

    public void setStatus(AgendaStatus status) {
        this.status = status;
    }
}
