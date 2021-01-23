package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.domain.Session;
import com.github.votingsessionmanager.domain.Vote;
import com.github.votingsessionmanager.service.VotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/session")
public class SessionController {

    private VotingService votingService;

    @Autowired
    public SessionController(VotingService votingService) {
        this.votingService = votingService;
    }

    @GetMapping
    @ResponseBody
    public List<Session> findAll() {
        return votingService.findAllSessions();
    }

    @GetMapping("/{sessionId}")
    @ResponseBody
    public Session findById(@PathVariable String sessionId) {
        return votingService.findSessionById(sessionId);
    }

    @GetMapping("/agenda/{agendaId}")
    @ResponseBody
    public List<Session> findByAgendaId(@PathVariable String agendaId) {
        return votingService.findSessionsByAgendaId(agendaId);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Session createSession(@RequestBody Session session) {
        return votingService.createSession(session);
    }

    @PostMapping("/vote/{agendaId}")
    @ResponseBody
    public Vote registerVote(@RequestBody Vote vote, @PathVariable String agendaId) {
        return votingService.registerVote(vote, agendaId);
    }
}
