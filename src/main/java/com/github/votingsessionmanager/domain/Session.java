package com.github.votingsessionmanager.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
public class Session {

    @Id
    private String id;

    private String agendaId;
    private long duration;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Vote> votes;
    private SessionStatus status;

    public Session() {}

    public Session(String agendaId, long duration, LocalDateTime startDate) {
        this.agendaId = agendaId;
        this.duration = duration;
        this.startDate = startDate;
    }

    public Session(String id, String agendaId, long duration, SessionStatus status) {
        this.id = id;
        this.agendaId = agendaId;
        this.duration = duration;
        this.status = status;
    }

    public Session(String agendaId, long duration) {
        this.agendaId = agendaId;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgendaId() {
        return agendaId;
    }

    public void setAgendaId(String agendaId) {
        this.agendaId = agendaId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }
}
