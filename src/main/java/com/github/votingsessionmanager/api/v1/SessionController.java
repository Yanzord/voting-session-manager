package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.domain.Session;
import com.github.votingsessionmanager.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/session")
public class SessionController {

    private SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    @ResponseBody
    public List<Session> findAll() {
        return sessionService.findAll();
    }

    @GetMapping("/{sessionId}")
    @ResponseBody
    public Session findById(@PathVariable String sessionId) {
        return sessionService.findById(sessionId);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Session createSession(@RequestBody Session session) {
        return sessionService.createSession(session);
    }

}