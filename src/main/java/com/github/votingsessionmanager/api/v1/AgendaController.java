package com.github.votingsessionmanager.api.v1;

import com.github.votingsessionmanager.domain.Agenda;
import com.github.votingsessionmanager.domain.AgendaStatusDTO;
import com.github.votingsessionmanager.service.VotingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(
            summary = "Find all agendas",
            description = "Use to find all agendas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success")
            }
    )
    @GetMapping
    @ResponseBody
    public List<Agenda> findAll() {
        return service.findAllAgendas();
    }

    @Operation(
            summary = "Find agenda by ID",
            description = "Use to find an agenda by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Agenda ID not found")
            }
    )
    @GetMapping("/{agendaId}")
    @ResponseBody
    public Agenda findById(@PathVariable String agendaId) {
        return service.findAgendaById(agendaId);
    }

    @Operation(
            summary = "Create new agenda",
            description = "Use to create a new agenda",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Missing required fields")
            }
    )
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Agenda createAgenda(@RequestBody Agenda agenda) {
        return service.createAgenda(agenda);
    }

    @Operation(
            summary = "Update agenda status",
            description = "Use to update the agenda status to CLOSED",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Invalid status"),
                    @ApiResponse(responseCode = "404", description = "Agenda ID not found")
            }
    )
    @PatchMapping("/{agendaId}")
    @ResponseBody
    public Agenda closeAgenda(@RequestBody AgendaStatusDTO update, @PathVariable String agendaId) {
        return service.closeAgenda(update, agendaId);
    }
}
