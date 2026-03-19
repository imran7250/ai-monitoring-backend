//package com.imran.aimonitoring.service;
//
//import java.time.LocalDateTime;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.imran.aimonitoring.repository.IncidentRepository;
//
//@Service
//public class IncidentRetentionService {
//
//    private static final int RETENTION_DAYS = 60;     
//
//    private final IncidentRepository incidentRepository;
//
//    public IncidentRetentionService(IncidentRepository incidentRepository) {
//        this.incidentRepository = incidentRepository;
//    }
//
//    @Transactional
//    public void cleanupOldIncidents() {
//
//        LocalDateTime cutoff =
//                LocalDateTime.now().minusDays(RETENTION_DAYS);
//
//        incidentRepository.deleteOlderThan(cutoff);
//
//        System.out.println("Old incidents cleaned before: " + cutoff);
//    }
//}

package com.imran.aimonitoring.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.repository.IncidentRepository;

// ✅ IMPROVEMENT #2 — Replaced System.out.println with proper Logger

@Service
public class IncidentRetentionService {

    // ✅ Proper Logger
    private static final Logger log = LoggerFactory.getLogger(IncidentRetentionService.class);

    private static final int RETENTION_DAYS = 60;

    private final IncidentRepository incidentRepository;

    public IncidentRetentionService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public void cleanupOldIncidents() {

        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);

        incidentRepository.deleteOlderThan(cutoff);

        // ✅ Use logger instead of System.out.println
        log.info("Old incidents cleaned before cutoff={}", cutoff);
    }
}