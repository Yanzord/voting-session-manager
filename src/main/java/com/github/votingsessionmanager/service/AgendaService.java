package com.github.votingsessionmanager.service;

import com.github.votingsessionmanager.domain.Agenda;
import com.github.votingsessionmanager.domain.AgendaStatus;
import com.github.votingsessionmanager.exception.IdNotFoundException;
import com.github.votingsessionmanager.repository.AgendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaService {

    private AgendaRepository agendaRepository;

    @Autowired
    public AgendaService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public List<Agenda> findAll() {
        return agendaRepository.findAll();
    }

    public Agenda findById(String agendaId) {
        return agendaRepository
                .findById(agendaId)
                .orElseThrow(IdNotFoundException::new);
    }

    public Agenda save(Agenda agenda) {
        agenda.setStatus(AgendaStatus.OPENED);
        return agendaRepository.save(agenda);
    }
}
