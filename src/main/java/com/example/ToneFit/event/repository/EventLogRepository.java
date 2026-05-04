package com.example.ToneFit.event.repository;

import com.example.ToneFit.event.model.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
}
