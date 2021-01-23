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

    private AgendaService service;

    @Autowired
    public AgendaController(AgendaService service) {
        this.service = service;
    }

    @GetMapping
    @ResponseBody
    public List<Agenda> findAll() {
        return service.findAll();
    }

    @GetMapping("/{agendaId}")
    @ResponseBody
    public Agenda findById(@PathVariable String agendaId) {
        return service.findById(agendaId);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Agenda save(@RequestBody Agenda agenda) {
        return service.save(agenda);
    }
}
