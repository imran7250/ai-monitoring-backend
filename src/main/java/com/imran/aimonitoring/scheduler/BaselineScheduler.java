//package com.imran.aimonitoring.scheduler;
//
//import java.util.List;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.imran.aimonitoring.entity.MonitoredService;
//import com.imran.aimonitoring.repository.MonitoredServiceRepository;
//import com.imran.aimonitoring.service.BaselineTrainingService;
//
//import lombok.RequiredArgsConstructor;
//
//@Component
//@RequiredArgsConstructor
//public class BaselineScheduler {
//
//    private final MonitoredServiceRepository serviceRepo;
//    private final BaselineTrainingService baselineService;
//
//    @Scheduled(fixedRate = 3600000) // every 1 hour
////    @Scheduled(fixedRate = 60000) // every 1 minute (testing)
//    public void retrainBaselines() {
//
//        List<MonitoredService> services = serviceRepo.findAll();
//
//        for (MonitoredService service : services) {
//            baselineService.trainBaseline(service.getId());
//        }
//
//        System.out.println("Baselines recalculated");
//    }
//}


package com.imran.aimonitoring.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.service.BaselineTrainingService;

import lombok.RequiredArgsConstructor;

// ✅ IMPROVEMENT #2 — Replaced System.out.println with proper Logger

@Component
@RequiredArgsConstructor
public class BaselineScheduler {

    // ✅ Proper Logger
    private static final Logger log = LoggerFactory.getLogger(BaselineScheduler.class);

    private final MonitoredServiceRepository serviceRepo;
    private final BaselineTrainingService baselineService;

    @Scheduled(fixedRate = 3600000) // every 1 hour
    public void retrainBaselines() {

        List<MonitoredService> services = serviceRepo.findAll();

        for (MonitoredService service : services) {
            baselineService.trainBaseline(service.getId());
        }

        // ✅ Use logger instead of System.out.println("Baselines recalculated")
        log.info("Baselines recalculated for {} services", services.size());
    }
}