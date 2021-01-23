package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.domain.Agenda;
import com.github.votingsessionmanager.domain.AgendaStatusDTO;
import com.github.votingsessionmanager.service.VotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/agenda")
public class AgendaController {

    private VotingService service;

    @Autowired
    public AgendaController(VotingService service) {
        this.service = service;
    }

    @GetMapping
    @ResponseBody
    public List<Agenda> findAll() {
        return service.findAllAgendas();
    }

    @GetMapping("/{agendaId}")
    @ResponseBody
    public Agenda findById(@PathVariable String agendaId) {
        return service.findAgendaById(agendaId);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Agenda createAgenda(@RequestBody Agenda agenda) {
        return service.createAgenda(agenda);
    }

    @PatchMapping("/{agendaId}")
    @ResponseBody
    public Agenda closeAgenda(@RequestBody AgendaStatusDTO update, @PathVariable String agendaId) {
        return service.closeAgenda(update, agendaId);
    }
}
