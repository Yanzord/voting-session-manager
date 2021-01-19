package com.github.votingsessionmanager.repository;

import com.github.votingsessionmanager.domain.Agenda;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendaRepository extends MongoRepository<Agenda, String> {
}
