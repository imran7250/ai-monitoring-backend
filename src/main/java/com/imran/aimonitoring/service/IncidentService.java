package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.dto.RecentIncidentResponse;
import com.imran.aimonitoring.entity.Incident;
import com.imran.aimonitoring.entity.IncidentStatus;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.exception.ResourceNotFoundException;
import com.imran.aimonitoring.repository.IncidentRepository;

@Transactional(readOnly = true)
@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public void handleServiceDown(MonitoredService service, String reason) {

        Optional<Incident> existing =
                incidentRepository.findByServiceAndStatus(service, IncidentStatus.OPEN);

        if (existing.isPresent()) {
            return;
        }

        Incident incident = Incident.builder()
                .service(service)
                .status(IncidentStatus.OPEN)
                .reason(reason)
                .startedAt(LocalDateTime.now())
                .build();

        incidentRepository.save(incident);
    }

    @Transactional
    public void handleServiceUp(MonitoredService service) {

        Optional<Incident> existing =
                incidentRepository.findByServiceAndStatus(service, IncidentStatus.OPEN);

        if (existing.isEmpty()) {
            return;
        }

        Incident incident = existing.get();

        if (incident.getStatus() == IncidentStatus.ACKNOWLEDGED) {
            return;
        }

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incidentRepository.save(incident);
    }

    @Transactional(readOnly = true)
    public List<RecentIncidentResponse> getOpenIncidents(String userEmail, boolean isAdmin) {

        List<Incident> incidents;

        if (isAdmin) {
            incidents = incidentRepository.findByStatusInOrderByStartedAtDesc(
                    List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED)
            );
        } else {
            incidents = incidentRepository.findActiveByOwner(
                    List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED),
                    userEmail
            );
        }

        return incidents.stream()
                .map(i -> RecentIncidentResponse.builder()
                        .incidentId(i.getId())
                        .serviceId(i.getService().getId())
                        .serviceName(i.getService().getName())
                        .reason(i.getReason())
                        .status(i.getStatus().name())
                        .startedAt(i.getStartedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Incident> getServiceIncidents(Long serviceId, String userEmail) {

        return incidentRepository.findServiceIncidentsForOwner(serviceId, userEmail);
    }

    @Transactional
    public Incident manualResolve(Long incidentId, String userEmail, boolean isAdmin) {

        // ✅ FIXED
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));

        if (!isAdmin) {
            String ownerEmail = incident.getService()
                    .getProject()
                    .getOwner()
                    .getEmail();

            if (!ownerEmail.equals(userEmail)) {
                throw new AccessDeniedException("You do not own this incident");
            }
        }

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            return incident;
        }

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident acknowledgeIncident(Long incidentId, String userEmail, boolean isAdmin) {

        // ✅ FIXED
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));

        if (!isAdmin) {
            String ownerEmail = incident.getService()
                    .getProject()
                    .getOwner()
                    .getEmail();

            if (!ownerEmail.equals(userEmail)) {
                throw new AccessDeniedException("You do not own this incident");
            }
        }

        if (incident.getStatus() == IncidentStatus.ACKNOWLEDGED) {
            return incident;
        }

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            return incident;
        }

        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        return incidentRepository.save(incident);
    }

    public Incident getIncidentById(Long incidentId, String userEmail, boolean isAdmin) {

        // ✅ FIXED
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));

        if (isAdmin) {
            return incident;
        }

        if (!incident.getService()
                .getProject()
                .getOwner()
                .getEmail()
                .equals(userEmail)) {
            throw new AccessDeniedException("You do not own this incident");
        }

        return incident;
    }
}