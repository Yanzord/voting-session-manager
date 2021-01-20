package com.github.votingsessionmanager.service;

import com.github.votingsessionmanager.domain.Session;
import com.github.votingsessionmanager.domain.SessionStatus;
import com.github.votingsessionmanager.exception.IdNotFoundException;
import com.github.votingsessionmanager.exception.RequiredAgendaIdException;
import com.github.votingsessionmanager.exception.SessionCreatedException;
import com.github.votingsessionmanager.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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


    public Session createSession(Session session) {
        if (session.getAgendaId() == null) {
            throw new RequiredAgendaIdException();
        }

        Optional<Session> sessionFound = Optional.of(sessionRepository.findByAgendaId(session.getAgendaId()));

        if (sessionFound.isPresent()) {
            throw new SessionCreatedException();
        }

        LocalDateTime startDate = LocalDateTime.now();
        session.setStartDate(startDate);

        LocalDateTime endDate = session.getStartDate().plusMinutes(session.getDuration());
        session.setEndDate(endDate);
        session.setStatus(SessionStatus.OPENED);

        if (session.getDuration() == 0)
            session.setDuration(1);

        return sessionRepository.save(session);
    }
}
