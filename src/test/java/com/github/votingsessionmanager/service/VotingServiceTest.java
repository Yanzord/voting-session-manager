package com.github.votingsessionmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.votingsessionmanager.domain.*;
import com.github.votingsessionmanager.exception.*;
import com.github.votingsessionmanager.feign.CPFValidator;
import com.github.votingsessionmanager.repository.AgendaRepository;
import com.github.votingsessionmanager.repository.SessionRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {"springdoc.api-docs.enabled=false"})
public class VotingServiceTest {

    @Autowired
    private ObjectMapper mapper;

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private CPFValidator cpfValidator;

    @InjectMocks
    private VotingService service;

    @Test
    public void shouldFindAllAgendas() {
        List<Agenda> expected = new ArrayList<>();
        expected.add(new Agenda("1", "Test 1"));
        expected.add(new Agenda("2", "Test 2"));
        expected.add(new Agenda("3", "Test 3"));

        given(agendaRepository.findAll()).willReturn(expected);

        List<Agenda> actual = service.findAllAgendas();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAgendaById() {
        String agendaId = "1";
        Agenda expected = new Agenda(agendaId, "Test");

        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(expected));

        Agenda actual = service.findAgendaById(agendaId);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDescription(), actual.getDescription());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToFindAgendaAndIdIsNotFound() {
        String agendaId = "1";

        given(agendaRepository.findById(agendaId)).willReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> service.findAgendaById(agendaId));
    }

    @Test
    public void shouldCreateAgenda() {
        Agenda dummyAgenda = new Agenda();
        dummyAgenda.setDescription("DummyAgenda");

        Agenda expected = new Agenda();
        expected.setDescription("DummyAgenda");
        expected.setStatus(AgendaStatus.OPENED);

        given(agendaRepository.save(BDDMockito.any(Agenda.class))).willReturn(expected);

        Agenda actual = service.createAgenda(dummyAgenda);

        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToCreateAgendaWithInvalidDescription() {
        Agenda dummyAgenda = new Agenda();

        assertThrows(RequiredFieldException.class, () -> service.createAgenda(dummyAgenda));

        dummyAgenda.setDescription("");
        assertThrows(RequiredFieldException.class, () -> service.createAgenda(dummyAgenda));
    }

    @Test
    public void shouldCloseAgendaAndSessionAndGenerateResult() {
        String agendaId = "1";

        List<Session> sessions = new ArrayList<>();
        Session session = new Session("2", agendaId, 2, SessionStatus.OPENED);
        session.setEndDate(LocalDateTime.now().minusMinutes(1));
        sessions.add(session);

        AgendaStatusDTO update = new AgendaStatusDTO();
        update.setAgendaStatus(AgendaStatus.CLOSED);

        Agenda dummyAgenda = new Agenda(agendaId, "Test");
        dummyAgenda.setStatus(AgendaStatus.OPENED);

        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(dummyAgenda));
        given(sessionRepository.findByAgendaId(agendaId)).willReturn(sessions);
        given(sessionRepository.save(BDDMockito.any(Session.class))).willReturn(session);
        given(sessionRepository.findById(session.getId())).willReturn(Optional.of(session));
        given(agendaRepository.save(BDDMockito.any(Agenda.class))).willReturn(dummyAgenda);

        Agenda actual = service.closeAgenda(update, agendaId);
        Session updatedSession = sessionRepository.findById(session.getId()).get();

        assertEquals(agendaId, actual.getId());
        assertEquals("Test", actual.getDescription());
        assertEquals(AgendaStatus.CLOSED, actual.getStatus());
        assertEquals("EMPATE", actual.getResult());
        assertEquals(SessionStatus.CLOSED, updatedSession.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToCloseAgendaWithInvalidStatus() {
        AgendaStatusDTO update = new AgendaStatusDTO();

        assertThrows(AgendaStatusException.class, () -> service.closeAgenda(update, "1"));

        update.setAgendaStatus(AgendaStatus.OPENED);

        assertThrows(AgendaStatusException.class, () -> service.closeAgenda(update, "1"));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToCloseAgendaWithInvalidId() {
        AgendaStatusDTO update = new AgendaStatusDTO();
        update.setAgendaStatus(AgendaStatus.CLOSED);

        assertThrows(RequiredFieldException.class, () -> service.closeAgenda(update, ""));
        assertThrows(RequiredFieldException.class, () -> service.closeAgenda(update, null));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToCloseAgendaAndAgendaIsAlreadyClosed() {
        AgendaStatusDTO update = new AgendaStatusDTO();
        update.setAgendaStatus(AgendaStatus.CLOSED);

        String agendaId = "1";

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.CLOSED);

        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        assertThrows(AgendaStatusException.class, () -> service.closeAgenda(update, agendaId));
    }

    @Test
    public void shouldFindAllSessions() {
        String agendaId = "1";
        long duration = 1;
        List<Session> expected = new ArrayList<>();
        expected.add(new Session("1", agendaId, duration, SessionStatus.CLOSED));
        expected.add(new Session("2", agendaId, duration, SessionStatus.CLOSED));
        expected.add(new Session("3", agendaId, duration, SessionStatus.OPENED));

        given(sessionRepository.findAll()).willReturn(expected);

        List<Session> actual = service.findAllSessions();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAndUpdateSessionById() {
        String sessionId = "1";
        String agendaId = "1";

        Session expected = new Session(sessionId, agendaId, 1, SessionStatus.OPENED);
        expected.setEndDate(LocalDateTime.now().minusMinutes(1));

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.CLOSED);

        given(sessionRepository.findById(sessionId)).willReturn(Optional.of(expected));
        given(sessionRepository.save(BDDMockito.any(Session.class))).willReturn(expected);
        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        Session actual = service.findAndUpdateSessionById(sessionId);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAgendaId(), actual.getAgendaId());
        assertEquals(SessionStatus.CLOSED, actual.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToFindAndUpdateSessionWithInvalidId() {
        String sessionId = "1";

        given(sessionRepository.findById(sessionId)).willReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> service.findAndUpdateSessionById(sessionId));
    }

    @Test
    public void shouldFindAndUpdateSessionsByAgendaId() {
        String agendaId = "1";
        long duration = 1;

        Session session = new Session("1", agendaId, duration, SessionStatus.OPENED);
        session.setEndDate(LocalDateTime.now().minusMinutes(1));

        List<Session> expected = new ArrayList<>();
        expected.add(session);

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.CLOSED);

        given(sessionRepository.findByAgendaId(agendaId)).willReturn(expected);
        given(sessionRepository.save(BDDMockito.any(Session.class))).willReturn(session);
        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        List<Session> actual = service.findAndUpdateSessionsByAgendaId(agendaId);

        assertEquals(expected.get(0).getId(), actual.get(0).getId());
        assertEquals(expected.get(0).getId(), actual.get(0).getId());
        assertEquals(SessionStatus.CLOSED, actual.get(0).getStatus());
    }

    @Test
    public void shouldReturnEmptySessionListWhenTryingToFindSessionsByAgendaIdAndTheresNoSessionsCreated() {
        String agendaId = "1";

        given(sessionRepository.findByAgendaId(agendaId)).willReturn(new ArrayList<>());
        given(agendaRepository.findById(agendaId)).willReturn(Optional.empty());

        List<Session> sessions = service.findAndUpdateSessionsByAgendaId(agendaId);

        assertTrue(sessions.isEmpty());
    }

    @Test
    public void shouldCreateSession() {
        String sessionId = "1";
        String agendaId = "1";
        long duration = 2;

        Session expected = new Session(sessionId, agendaId, duration);

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(agendaId)).willReturn(new ArrayList<>());
        given(sessionRepository.save(BDDMockito.any(Session.class))).willReturn(expected);

        Session actual = service.createSession(expected);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAgendaId(), actual.getAgendaId());
        assertEquals(expected.getDuration(), actual.getDuration());
        assertEquals(SessionStatus.OPENED, actual.getStatus());
    }

    @Test
    public void shouldOpenSessionAndSetDefaultDurationTimeWhenNoneIsProvided() {
        String sessionId = "1";
        String agendaId = "1";
        long defaultDuration = 1;

        Session expected = new Session(sessionId, agendaId);

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(agendaId)).willReturn(new ArrayList<>());
        given(sessionRepository.save(BDDMockito.any(Session.class))).willReturn(expected);

        Session actual = service.createSession(expected);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAgendaId(), actual.getAgendaId());
        assertEquals(defaultDuration, actual.getDuration());
        assertEquals(SessionStatus.OPENED, actual.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToCreateSessionWithInvalidAgendaId() {
        Session session = new Session("1", "", 2);

        assertThrows(RequiredFieldException.class, () -> service.createSession(session));

        session.setAgendaId(null);

        assertThrows(RequiredFieldException.class, () -> service.createSession(session));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToCreateSessionWithClosedAgenda() {
        String agendaId = "1";

        Agenda agenda = new Agenda(agendaId, "test");
        agenda.setStatus(AgendaStatus.CLOSED);

        Session session = new Session("1", agendaId, 2);

        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        assertThrows(AgendaStatusException.class, () -> service.createSession(session));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToCreateSessionWithOpenedSessionInCourse() {
        String agendaId = "1";
        long duration = 2;

        Agenda agenda = new Agenda(agendaId, "test");
        agenda.setStatus(AgendaStatus.OPENED);

        Session openedSession = new Session("1", agendaId, duration, SessionStatus.OPENED);
        openedSession.setStartDate(LocalDateTime.now());
        openedSession.setEndDate(openedSession.getStartDate().plusMinutes(openedSession.getDuration()));

        List<Session> sessions = new ArrayList<>();
        sessions.add(openedSession);

        Session session = new Session("2", agendaId, duration);

        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(agendaId)).willReturn(sessions);

        assertThrows(SessionStatusException.class, () -> service.createSession(session));
    }

    @Test
    public void shouldRegisterVote() throws JsonProcessingException {
        String id = "1";
        String cpf = "123";

        Agenda agenda = new Agenda(id, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        Session openedSession = new Session(id, id, 2, SessionStatus.OPENED);
        openedSession.setStartDate(LocalDateTime.now());
        openedSession.setEndDate(openedSession.getStartDate().plusMinutes(openedSession.getDuration()));

        List<Session> sessions = new ArrayList<>();
        sessions.add(openedSession);

        Vote vote = new Vote(id, cpf, VoteOption.SIM);

        ObjectNode cpfValidatorResponse = mapper.readValue("{\"status\":\"ABLE_TO_VOTE\"}", ObjectNode.class);

        given(agendaRepository.findById(id)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(id)).willReturn(sessions);
        given(cpfValidator.validateCPF(cpf)).willReturn(cpfValidatorResponse);
        given(sessionRepository.save(BDDMockito.any(Session.class))).willReturn(openedSession);

        service.registerVote(vote, id);

        assertEquals(id, openedSession.getVotes().get(0).getMemberId());
        assertEquals(cpf, openedSession.getVotes().get(0).getMemberCPF());
        assertEquals(VoteOption.SIM, openedSession.getVotes().get(0).getVoteOption());
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithInvalidVoteOption() {
        Vote vote = new Vote();
        vote.setMemberId("1");
        vote.setMemberCPF("1");

        assertThrows(RequiredFieldException.class, () -> service.registerVote(vote, "1"));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithInvalidMemberId() {
        Vote vote = new Vote();
        vote.setMemberCPF("1");
        vote.setVoteOption(VoteOption.SIM);

        assertThrows(RequiredFieldException.class, () -> service.registerVote(vote, "1"));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithInvalidMemberCpf() {
        Vote vote = new Vote();
        vote.setMemberId("1");
        vote.setVoteOption(VoteOption.SIM);

        assertThrows(RequiredFieldException.class, () -> service.registerVote(vote, "1"));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithInvalidAgendaId() {
        Vote vote = new Vote("1", "1", VoteOption.SIM);

        assertThrows(RequiredFieldException.class, () -> service.registerVote(vote, ""));
        assertThrows(RequiredFieldException.class, () -> service.registerVote(vote, null));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithAgendaIdNotFound() {
        String agendaId = "1";

        Vote vote = new Vote("1", "1", VoteOption.SIM);

        given(agendaRepository.findById(agendaId)).willReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> service.registerVote(vote, agendaId));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithClosedAgenda() {
        String id = "1";
        String cpf = "123";

        Agenda agenda = new Agenda(id, "Test");
        agenda.setStatus(AgendaStatus.CLOSED);

        Vote vote = new Vote(id, cpf, VoteOption.SIM);

        given(agendaRepository.findById(id)).willReturn(Optional.of(agenda));

        assertThrows(AgendaStatusException.class, () -> service.registerVote(vote, id));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithClosedSession() {
        String id = "1";
        String cpf = "123";

        Agenda agenda = new Agenda(id, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        Session closedSession = new Session(id, id, 2, SessionStatus.CLOSED);
        closedSession.setStartDate(LocalDateTime.now().minusMinutes(5));
        closedSession.setEndDate(closedSession.getStartDate().plusMinutes(closedSession.getDuration()));

        List<Session> sessions = new ArrayList<>();
        sessions.add(closedSession);

        Vote vote = new Vote(id, cpf, VoteOption.SIM);

        given(agendaRepository.findById(id)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(id)).willReturn(sessions);

        assertThrows(SessionStatusException.class, () -> service.registerVote(vote, id));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithSameMemberId() throws JsonProcessingException {
        String id = "1";
        String cpf = "123";

        Agenda agenda = new Agenda(id, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote(id, "321", VoteOption.SIM));

        Session openedSession = new Session(id, id, 2, SessionStatus.OPENED);
        openedSession.setStartDate(LocalDateTime.now());
        openedSession.setEndDate(openedSession.getStartDate().plusMinutes(openedSession.getDuration()));
        openedSession.setVotes(votes);

        List<Session> sessions = new ArrayList<>();
        sessions.add(openedSession);

        Vote vote = new Vote(id, cpf, VoteOption.SIM);

        given(agendaRepository.findById(id)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(id)).willReturn(sessions);

        assertThrows(InvalidVoteException.class, () -> service.registerVote(vote, id));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithSameMemberCpf() throws JsonProcessingException {
        String id = "1";
        String cpf = "123";

        Agenda agenda = new Agenda(id, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote("2", cpf, VoteOption.SIM));

        Session openedSession = new Session(id, id, 2, SessionStatus.OPENED);
        openedSession.setStartDate(LocalDateTime.now());
        openedSession.setEndDate(openedSession.getStartDate().plusMinutes(openedSession.getDuration()));
        openedSession.setVotes(votes);

        List<Session> sessions = new ArrayList<>();
        sessions.add(openedSession);

        Vote vote = new Vote(id, cpf, VoteOption.SIM);

        given(agendaRepository.findById(id)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(id)).willReturn(sessions);

        assertThrows(InvalidVoteException.class, () -> service.registerVote(vote, id));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithUnableToVoteCpf() throws JsonProcessingException {
        String id = "1";
        String cpf = "123";

        Agenda agenda = new Agenda(id, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        Session openedSession = new Session(id, id, 2, SessionStatus.OPENED);
        openedSession.setStartDate(LocalDateTime.now());
        openedSession.setEndDate(openedSession.getStartDate().plusMinutes(openedSession.getDuration()));

        List<Session> sessions = new ArrayList<>();
        sessions.add(openedSession);

        Vote vote = new Vote(id, cpf, VoteOption.SIM);

        ObjectNode cpfValidatorResponse = mapper.readValue("{\"status\":\"UNABLE_TO_VOTE\"}", ObjectNode.class);

        given(agendaRepository.findById(id)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(id)).willReturn(sessions);
        given(cpfValidator.validateCPF(cpf)).willReturn(cpfValidatorResponse);

        assertThrows(InvalidVoteException.class, () -> service.registerVote(vote, id));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRegisterVoteWithInvalidCpf() {
        String id = "1";
        String cpf = "123";

        Agenda agenda = new Agenda(id, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        Session openedSession = new Session(id, id, 2, SessionStatus.OPENED);
        openedSession.setStartDate(LocalDateTime.now());
        openedSession.setEndDate(openedSession.getStartDate().plusMinutes(openedSession.getDuration()));

        List<Session> sessions = new ArrayList<>();
        sessions.add(openedSession);

        Vote vote = new Vote(id, cpf, VoteOption.SIM);

        given(agendaRepository.findById(id)).willReturn(Optional.of(agenda));
        given(sessionRepository.findByAgendaId(id)).willReturn(sessions);
        given(cpfValidator.validateCPF(cpf)).willThrow(FeignException.NotFound.class);

        assertThrows(InvalidVoteException.class, () -> service.registerVote(vote, id));
    }

    @Test
    public void shouldCalculateResultSIM() {
        String agendaId = "1";

        List<Vote> firstVotes = new ArrayList<>();
        firstVotes.add(new Vote("1", "123", VoteOption.SIM));
        firstVotes.add(new Vote("2", "1234", VoteOption.SIM));

        Session firstSession = new Session("1", agendaId, 2, SessionStatus.CLOSED);
        firstSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        firstSession.setEndDate(firstSession.getStartDate().plusMinutes(firstSession.getDuration()));
        firstSession.setVotes(firstVotes);

        List<Vote> secondVotes = new ArrayList<>();
        secondVotes.add(new Vote("3", "321", VoteOption.SIM));
        secondVotes.add(new Vote("4", "4321", VoteOption.NAO));
        Session secondSession = new Session("2", agendaId, 2, SessionStatus.CLOSED);
        secondSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        secondSession.setEndDate(secondSession.getStartDate().plusMinutes(secondSession.getDuration()));
        secondSession.setVotes(secondVotes);

        List<Session> sessions = new ArrayList<>();
        sessions.add(firstSession);
        sessions.add(secondSession);

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        given(sessionRepository.findByAgendaId(agendaId)).willReturn(sessions);
        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        String result = service.calculateResult(agenda);

        assertEquals("SIM", result);
    }

    @Test
    public void shouldCalculateResultNAO() {
        String agendaId = "1";

        List<Vote> firstVotes = new ArrayList<>();
        firstVotes.add(new Vote("1", "123", VoteOption.NAO));
        firstVotes.add(new Vote("2", "1234", VoteOption.NAO));

        Session firstSession = new Session("1", agendaId, 2, SessionStatus.CLOSED);
        firstSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        firstSession.setEndDate(firstSession.getStartDate().plusMinutes(firstSession.getDuration()));
        firstSession.setVotes(firstVotes);

        List<Vote> secondVotes = new ArrayList<>();
        secondVotes.add(new Vote("3", "321", VoteOption.SIM));
        secondVotes.add(new Vote("4", "4321", VoteOption.NAO));
        Session secondSession = new Session("2", agendaId, 2, SessionStatus.CLOSED);
        secondSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        secondSession.setEndDate(secondSession.getStartDate().plusMinutes(secondSession.getDuration()));
        secondSession.setVotes(secondVotes);

        List<Session> sessions = new ArrayList<>();
        sessions.add(firstSession);
        sessions.add(secondSession);

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        given(sessionRepository.findByAgendaId(agendaId)).willReturn(sessions);
        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        String result = service.calculateResult(agenda);

        assertEquals("NAO", result);
    }

    @Test
    public void shouldCalculateResultEMPATE() {
        String agendaId = "1";

        List<Vote> firstVotes = new ArrayList<>();
        firstVotes.add(new Vote("1", "123", VoteOption.NAO));
        firstVotes.add(new Vote("2", "1234", VoteOption.NAO));

        Session firstSession = new Session("1", agendaId, 2, SessionStatus.CLOSED);
        firstSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        firstSession.setEndDate(firstSession.getStartDate().plusMinutes(firstSession.getDuration()));
        firstSession.setVotes(firstVotes);

        List<Vote> secondVotes = new ArrayList<>();
        secondVotes.add(new Vote("3", "321", VoteOption.SIM));
        secondVotes.add(new Vote("4", "4321", VoteOption.SIM));
        Session secondSession = new Session("2", agendaId, 2, SessionStatus.CLOSED);
        secondSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        secondSession.setEndDate(secondSession.getStartDate().plusMinutes(secondSession.getDuration()));
        secondSession.setVotes(secondVotes);

        List<Session> sessions = new ArrayList<>();
        sessions.add(firstSession);
        sessions.add(secondSession);

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        given(sessionRepository.findByAgendaId(agendaId)).willReturn(sessions);
        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        String result = service.calculateResult(agenda);

        assertEquals("EMPATE", result);
    }

    @Test
    public void shouldCalculateResultEMPATEWithNoVotes() {
        String agendaId = "1";

        Session firstSession = new Session("1", agendaId, 2, SessionStatus.CLOSED);
        firstSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        firstSession.setEndDate(firstSession.getStartDate().plusMinutes(firstSession.getDuration()));

        Session secondSession = new Session("2", agendaId, 2, SessionStatus.CLOSED);
        secondSession.setStartDate(LocalDateTime.now().minusMinutes(10));
        secondSession.setEndDate(secondSession.getStartDate().plusMinutes(secondSession.getDuration()));

        List<Session> sessions = new ArrayList<>();
        sessions.add(firstSession);
        sessions.add(secondSession);

        Agenda agenda = new Agenda(agendaId, "Test");
        agenda.setStatus(AgendaStatus.OPENED);

        given(sessionRepository.findByAgendaId(agendaId)).willReturn(sessions);
        given(agendaRepository.findById(agendaId)).willReturn(Optional.of(agenda));

        String result = service.calculateResult(agenda);

        assertEquals("EMPATE", result);
    }
}
