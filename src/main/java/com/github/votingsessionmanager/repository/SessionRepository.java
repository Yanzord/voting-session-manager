package com.github.votingsessionmanager.repository;

import com.github.votingsessionmanager.domain.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByAgendaId(String agendaId);
}
