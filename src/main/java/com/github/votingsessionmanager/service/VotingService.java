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
import java.util.Objects;
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
        if (agendaId == null || agendaId.isEmpty()) {
            throw new RequiredFieldException("Agenda Id is required.");
        }

        return agendaRepository
                .findById(agendaId)
                .orElseThrow(IdNotFoundException::new);
    }

    public Agenda createAgenda(Agenda agenda) {
        if(agenda.getDescription() == null || agenda.getDescription().isEmpty()) {
            throw new RequiredFieldException("Agenda description is required.");
        }

        agenda.setStatus(AgendaStatus.OPENED);
        return agendaRepository.save(agenda);
    }

    public Agenda closeAgenda(AgendaStatusDTO update, String agendaId) {
        if(update.getAgendaStatus() == null || !update.getAgendaStatus().equals(AgendaStatus.CLOSED)) {
            throw new AgendaStatusException("Invalid agenda status.");
        }

        Agenda agenda = findAgendaById(agendaId);
        validateAgenda(agenda);

        agenda.setStatus(update.getAgendaStatus());
        agenda.setResult(calculateResult(agenda));

        agendaRepository.save(agenda);

        findAndUpdateSessionsByAgendaId(agendaId);

        return agenda;
    }

    public List<Session> findAllSessions() {
        return sessionRepository.findAll();
    }

    public Session findAndUpdateSessionById(String sessionId) {
        return sessionRepository
                .findById(sessionId)
                .map(this::updateSession)
                .orElseThrow(IdNotFoundException::new);
    }

    public List<Session> findAndUpdateSessionsByAgendaId(String agendaId) {
        return sessionRepository.findByAgendaId(agendaId)
                .stream()
                .map(this::updateSession)
                .collect(Collectors.toList());
    }

    public Session createSession(Session session) {
        validateAgenda(findAgendaById(session.getAgendaId()));
        findAndUpdateSessionsByAgendaId(session.getAgendaId())
                .stream()
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

    public Vote registerVote(Vote vote, String agendaId) {
        validateVote(vote);
        validateAgenda(findAgendaById(agendaId));

        Session session = findAndUpdateSessionsByAgendaId(agendaId)
                .stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getStatus().equals(SessionStatus.OPENED))
                .findAny()
                .orElseThrow(() -> new SessionStatusException("There's no opened session for the given agenda."));

        List<Vote> votes = Optional.ofNullable(session.getVotes()).orElse(new ArrayList<>());

        boolean isVoteAlreadyRegistered = votes
                .stream()
                .anyMatch(v -> v.getMemberId().equals(vote.getMemberId()) || v.getMemberCPF().equals(vote.getMemberCPF()));

        if (isVoteAlreadyRegistered) {
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

    public String calculateResult(Agenda agenda) {
        List<Session> sessions = findAndUpdateSessionsByAgendaId(agenda.getId());
        System.out.println(sessions);

        long totalYes = sessions.stream()
                .map(session -> {
                    List<Vote> votes = Optional.ofNullable(session.getVotes()).orElse(new ArrayList<>());

                    return votes.stream()
                            .filter(vote -> vote.getVoteOption().equals(VoteOption.SIM))
                            .count();
                })
                .reduce(0L, Long::sum);

        long totalNo = sessions.stream()
                .map(session -> {
                    List<Vote> votes = Optional.ofNullable(session.getVotes()).orElse(new ArrayList<>());

                    return votes.stream()
                            .filter(vote -> vote.getVoteOption().equals(VoteOption.NAO))
                            .count();
                })
                .reduce(0L, Long::sum);

        if (totalYes == totalNo) {
            return "EMPATE";
        }

        return totalYes > totalNo ? VoteOption.SIM.toString() : VoteOption.NAO.toString();
    }

    private void validateAgenda(Agenda agenda) {
        if (agenda.getStatus().equals(AgendaStatus.CLOSED)) {
            throw new AgendaStatusException("Agenda is closed.");
        }
    }

    private Session updateSession(Session session) {
        boolean isSessionExpired = session.getEndDate().isBefore(LocalDateTime.now());
        boolean isClosedAgenda = findAgendaById(session.getAgendaId()).getStatus().equals(AgendaStatus.CLOSED);

        if ((isSessionExpired || isClosedAgenda) && session.getStatus().equals(SessionStatus.OPENED)) {
            session.setStatus(SessionStatus.CLOSED);
            return sessionRepository.save(session);
        }

        return session;
    }

    private void validateVote(Vote vote) {
        if(vote.getVoteOption() == null) {
            throw new RequiredFieldException("Vote option is required.");
        }

        if(!vote.getVoteOption().equals(VoteOption.SIM) && !vote.getVoteOption().equals(VoteOption.NAO)) {
            throw new InvalidVoteException("Vote option must be SIM or NAO.");
        }

        if(vote.getMemberId() == null || vote.getMemberId().isEmpty()) {
            throw new RequiredFieldException("Member ID is required.");
        }

        if(vote.getMemberCPF() == null || vote.getMemberCPF().isEmpty()) {
            throw new RequiredFieldException("Member CPF is required.");
        }
    }
}
