package com.imran.aimonitoring.entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alert_rules")
@Getter
@Setter
@Data
@NoArgsConstructor     
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus triggerStatus;

    private boolean enabled = true;

    private LocalDateTime createdAt;

    // ✅ FIX 1 — LAZY → EAGER so ownership check works without open session
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    @JsonIgnoreProperties({"project"})
    private MonitoredService service;

    // ✅ FIX 2 — @JsonIgnore stops Jackson from touching this lazy
    // collection entirely. @JsonIgnoreProperties was not enough because
    // Jackson still tries to ACCESS the collection before checking
    // what properties to ignore, which triggers the lazy load error.
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "alert_rule_channels",
        joinColumns = @JoinColumn(name = "alert_rule_id"),
        inverseJoinColumns = @JoinColumn(name = "channel_id")
    )
    private Set<NotificationChannel> channels;
}


//package com.imran.aimonitoring.entity;
//
//import java.time.LocalDateTime;
//import java.util.Set;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "alert_rules")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//public class AlertRule {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private ServiceStatus triggerStatus;
//
//    private boolean enabled = true;
//
//    private LocalDateTime createdAt;
//
//    // 🔥 FIX: prevent lazy proxy serialization
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "service_id", nullable = false)
////    @JsonIgnoreProperties({"project"})
////    private MonitoredService service;
//    
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "service_id", nullable = false)
//    @JsonIgnoreProperties({"project"})
//    private MonitoredService service;
//
//    // 🔥 FIX: prevent circular relation
//    @ManyToMany
//    @JoinTable(
//        name = "alert_rule_channels",
//        joinColumns = @JoinColumn(name = "alert_rule_id"),
//        inverseJoinColumns = @JoinColumn(name = "channel_id")
//    )
//    @JsonIgnoreProperties({"alertRules"})
//    private Set<NotificationChannel> channels; 
//}
