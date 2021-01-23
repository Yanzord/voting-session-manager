package com.github.votingsessionmanager.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.votingsessionmanager.domain.*;
import com.github.votingsessionmanager.exception.*;
import com.github.votingsessionmanager.feign.CPFValidator;
import com.github.votingsessionmanager.repository.AgendaRepository;
import com.github.votingsessionmanager.repository.SessionRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VotingService {

    private AgendaRepository agendaRepository;
    private SessionRepository sessionRepository;
    private CPFValidator cpfValidator;

    @Autowired
    public VotingService(AgendaRepository agendaRepository, SessionRepository sessionRepository, CPFValidator cpfValidator) {
        this.agendaRepository = agendaRepository;
        this.sessionRepository = sessionRepository;
        this.cpfValidator = cpfValidator;
    }

    public List<Agenda> findAllAgendas() {
        return agendaRepository.findAll();
    }

    public Agenda findAgendaById(String agendaId) {
        return agendaRepository
                .findById(agendaId)
                .orElseThrow(IdNotFoundException::new);
    }

    public Agenda createAgenda(Agenda agenda) {
        agenda.setStatus(AgendaStatus.OPENED);
        return agendaRepository.save(agenda);
    }

    public Agenda closeAgenda(AgendaStatusDTO update, String agendaId) {
        if(!update.getAgendaStatus().equals(AgendaStatus.CLOSED)) {
            throw new AgendaStatusException("Invalid agenda status.");
        }

        Agenda agenda = findAgendaById(agendaId);

        if(agenda.getStatus().equals(AgendaStatus.CLOSED)) {
            throw new AgendaStatusException("Agenda is already CLOSED.");
        }

        agenda.setStatus(update.getAgendaStatus());
        agenda.setResult(calculateResult(agenda));

        agendaRepository.save(agenda);

        findSessionsByAgendaId(agendaId)
                .stream()
                .map(this::updateSession);

        return agenda;
    }

    public List<Session> findAllSessions() {
        return sessionRepository.findAll();
    }

    public Session findSessionById(String sessionId) {
        return sessionRepository
                .findById(sessionId)
                .map(this::updateSession)
                .orElseThrow(IdNotFoundException::new);
    }

    public List<Session> findSessionsByAgendaId(String agendaId) {
        return sessionRepository.findByAgendaId(agendaId)
                .stream()
                .map(this::updateSession)
                .collect(Collectors.toList());
    }

    public Session createSession(Session session) {
        validateAgenda(session.getAgendaId());
        sessionRepository.findByAgendaIdAndStatus(session.getAgendaId(), SessionStatus.OPENED.toString())
                .stream()
                .map(this::updateSession)
                .filter(s -> s.getStatus().equals(SessionStatus.OPENED))
                .findAny()
                .ifPresent(a -> { throw new SessionStatusException("There's already an opened session for the given agenda."); });

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

        Agenda agenda = findAgendaById(agendaId);

        if (agenda.getStatus().equals(AgendaStatus.CLOSED)) {
            throw new AgendaStatusException("Agenda is closed.");
        }
    }

    private Session updateSession(Session session) {
        if (session.getEndDate().isBefore(LocalDateTime.now()) || findAgendaById(session.getAgendaId()).getStatus().equals(AgendaStatus.CLOSED)) {
            session.setStatus(SessionStatus.CLOSED);
            return sessionRepository.save(session);
        }

        return session;
    }

    public Vote registerVote(Vote vote, String agendaId) {
        validateAgenda(agendaId);

        Session session = sessionRepository.findByAgendaIdAndStatus(agendaId, SessionStatus.OPENED.toString())
                .stream()
                .map(this::updateSession)
                .filter(s -> s.getStatus().equals(SessionStatus.OPENED))
                .findAny()
                .orElseThrow(() -> new SessionStatusException("There's no opened session for the given agenda."));

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

    private String calculateResult(Agenda agenda) {
        List<Session> sessions = findSessionsByAgendaId(agenda.getId());

        long totalYes = sessions.stream()
                .map(session -> session.getVotes().stream()
                        .filter(vote -> vote.getVoteOption().equals(VoteOption.SIM))
                        .count())
                .reduce(0L, Long::sum);

        long totalNo = sessions.stream()
                .map(session -> session.getVotes().stream()
                    .filter(vote -> vote.getVoteOption().equals(VoteOption.NAO))
                    .count())
                .reduce(0L, Long::sum);

        if (totalYes == totalNo) {
            return "EMPATE";
        }

        return totalYes > totalNo ? VoteOption.SIM.toString() : VoteOption.NAO.toString();
    }
}
