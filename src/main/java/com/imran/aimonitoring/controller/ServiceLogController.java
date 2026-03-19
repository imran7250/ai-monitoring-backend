

package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.imran.aimonitoring.dto.ServiceLogResponse;
import com.imran.aimonitoring.service.ServiceLogService;

@RestController
@RequestMapping("/api/services/{serviceId}/logs")
public class ServiceLogController {

    private final ServiceLogService logService;

    public ServiceLogController(ServiceLogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public List<ServiceLogResponse> getLogs(@PathVariable Long serviceId) {

        String email = getCurrentUserEmail();

        return logService.getServiceLogs(serviceId, email)
                .stream()
                .map(l -> new ServiceLogResponse(
                        l.getId(),
                        l.getService().getId(),
                        l.getService().getName(),
                        l.getStatus().name(),
                        l.getResponseTimeMs(),
                        l.getErrorMessage(),
                        l.getCheckedAt()
                ))
                .toList();
    }

    private String getCurrentUserEmail() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
