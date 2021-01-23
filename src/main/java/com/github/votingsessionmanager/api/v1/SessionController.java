package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.domain.Session;
import com.github.votingsessionmanager.domain.Vote;
import com.github.votingsessionmanager.service.VotingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(
            summary = "Find all sessions",
            description = "Use to find all sessions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success")
            }
    )
    @GetMapping
    @ResponseBody
    public List<Session> findAll() {
        return votingService.findAllSessions();
    }

    @Operation(
            summary = "Find session by ID",
            description = "Use to find an session by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Session ID not found")
            }
    )
    @GetMapping("/{sessionId}")
    @ResponseBody
    public Session findById(@PathVariable String sessionId) {
        return votingService.findSessionById(sessionId);
    }

    @Operation(
            summary = "Find sessions by agenda ID",
            description = "Use to find all sessions created by agenda ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Agenda ID not found")
            }
    )
    @GetMapping("/agenda/{agendaId}")
    @ResponseBody
    public List<Session> findByAgendaId(@PathVariable String agendaId) {
        return votingService.findSessionsByAgendaId(agendaId);
    }

    @Operation(
            summary = "Create new session",
            description = "Use to create a new session",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Missing required fields, the agenda is CLOSED or there's already an opened session for the agenda"),
                    @ApiResponse(responseCode = "404", description = "Agenda ID not found")
            }
    )
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Session createSession(@RequestBody Session session) {
        return votingService.createSession(session);
    }

    @Operation(
            summary = "Register new vote",
            description = "Use to register a new vote on a opened session for the agenda",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Invalid vote, missing required fields or agenda is closed"),
                    @ApiResponse(responseCode = "404", description = "Agenda ID not found")
            }
    )
    @PatchMapping("/vote/{agendaId}")
    @ResponseBody
    public Vote registerVote(@RequestBody Vote vote, @PathVariable String agendaId) {
        return votingService.registerVote(vote, agendaId);
    }
}
