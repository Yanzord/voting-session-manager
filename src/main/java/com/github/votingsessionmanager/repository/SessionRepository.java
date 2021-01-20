package com.github.votingsessionmanager.repository;

import com.github.votingsessionmanager.domain.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<Session, String> {
    Session findByAgendaId(String agendaId);
}
