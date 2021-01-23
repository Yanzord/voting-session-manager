package com.github.votingsessionmanager.service;

import com.github.votingsessionmanager.domain.Agenda;
import com.github.votingsessionmanager.domain.AgendaStatus;
import com.github.votingsessionmanager.domain.Session;
import com.github.votingsessionmanager.domain.SessionStatus;
import com.github.votingsessionmanager.exception.ClosedAgendaException;
import com.github.votingsessionmanager.exception.IdNotFoundException;
import com.github.votingsessionmanager.exception.OpenedSessionException;
import com.github.votingsessionmanager.exception.RequiredAgendaIdException;
import com.github.votingsessionmanager.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionService {

    private SessionRepository sessionRepository;
    private AgendaService agendaService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, AgendaService agendaService) {
        this.sessionRepository = sessionRepository;
        this.agendaService = agendaService;
    }

    public List<Session> findAll() {
        return sessionRepository.findAll();
    }

    public Session findById(String sessionId) {
        return sessionRepository
                .findById(sessionId)
                .orElseThrow(IdNotFoundException::new);
    }

    public List<Session> findByAgendaId(String agendaId) {
        return sessionRepository.findByAgendaId(agendaId);
    }

    public Session createSession(Session session) {
        validateAgenda(session.getAgendaId());
        validateSessions(session.getAgendaId());

        if (session.getDuration() == 0) {
            session.setDuration(1);
        }

        session.setStartDate(LocalDateTime.now());

        LocalDateTime endDate = session.getStartDate().plusMinutes(session.getDuration());
        session.setEndDate(endDate);
        session.setStatus(SessionStatus.OPENED);

        return sessionRepository.save(session);
    }

    private void validateAgenda(String agendaId) {
        if (agendaId.isEmpty()) {
            throw new RequiredAgendaIdException();
        }

        Agenda agenda = agendaService.findById(agendaId);

        if (agenda.getStatus().equals(AgendaStatus.CLOSED)) {
            throw new ClosedAgendaException();
        }
    }

    private void validateSessions(String agendaId) {
        List<Session> sessions = sessionRepository.findByAgendaId(agendaId);

        sessions.stream()
                .filter(s -> s.getStatus().equals(SessionStatus.OPENED))
                .map(this::update)
                .filter(s -> s.getStatus().equals(SessionStatus.OPENED))
                .findAny()
                .ifPresent(a -> { throw new OpenedSessionException(); });
    }

    private Session update(Session session) {
        if (session.getEndDate().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.CLOSED);
            return sessionRepository.save(session);
        }

        return session;
    }


}
