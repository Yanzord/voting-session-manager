package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.domain.Agenda;
import com.github.votingsessionmanager.domain.AgendaStatus;
import com.github.votingsessionmanager.service.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/agenda")
public class AgendaController {

    private AgendaService agendaService;

    @Autowired
    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping
    @ResponseBody
    public List<Agenda> findAll() {
        return agendaService.findAll();
    }

    @GetMapping("/{agendaId}")
    @ResponseBody
    public Agenda findById(@PathVariable String agendaId) {
        return agendaService.findById(agendaId);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Agenda save(@RequestBody Agenda agenda) {
        agenda.setStatus(AgendaStatus.NEW);
        return agendaService.save(agenda);
    }
}
