package com.github.votingsessionmanager.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.votingsessionmanager.domain.*;
import com.github.votingsessionmanager.exception.*;
import com.github.votingsessionmanager.feign.CPFValidator;
import com.github.votingsessionmanager.repository.SessionRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class SessionService {

    private SessionRepository sessionRepository;
    private AgendaService agendaService;
    private CPFValidator cpfValidator;

    @Autowired
    public SessionService(SessionRepository sessionRepository, AgendaService agendaService, CPFValidator cpfValidator) {
        this.sessionRepository = sessionRepository;
        this.agendaService = agendaService;
        this.cpfValidator = cpfValidator;
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
        getUpdatedSessions(session.getAgendaId())
                .filter(s -> s.getStatus().equals(SessionStatus.OPENED))
                .findAny()
                .ifPresent(a -> { throw new OpenedSessionException(); });

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

    private Stream<Session> getUpdatedSessions(String agendaId) {
        return sessionRepository.findByAgendaIdAndStatus(agendaId, SessionStatus.OPENED.toString())
                .stream()
                .map(session -> {
                    if (session.getEndDate().isBefore(LocalDateTime.now())) {
                        session.setStatus(SessionStatus.CLOSED);
                        return sessionRepository.save(session);
                    }

                    return session;
                });
    }


    public Vote registerVote(String agendaId, Vote vote) {
        validateAgenda(agendaId);

        Session session = getUpdatedSessions(agendaId)
                .filter(s -> s.getStatus().equals(SessionStatus.OPENED))
                .findFirst()
                .orElseThrow(SessionNotOpenedException::new);

        List<Vote> votes = Optional.ofNullable(session.getVotes())
                .orElse(new ArrayList<>());

        if (votes.stream().anyMatch(v -> v.getMemberId().equals(vote.getMemberId()) || v.getMemberCPF().equals(vote.getMemberCPF()))) {
            throw new InvalidVoteException("Member already voted.");
        }

        try {
            ObjectNode json = cpfValidator.validateCPF(vote.getMemberCPF());

            String status = json.get("status").asText();

            if (status.equals("UNABLE_TO_VOTE")) {
                throw new InvalidVoteException("Member is unable to vote.");
            }

            votes.add(vote);
            session.setVotes(votes);
            sessionRepository.save(session);

            return vote;
        } catch (FeignException.NotFound e) {
            throw new InvalidVoteException("Invalid CPF.");
        }
    }
}
