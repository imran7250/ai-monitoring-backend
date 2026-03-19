package com.imran.aimonitoring.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type; // EMAIL, WEBHOOK

    @Column(nullable = false, length = 1000)
    private String target; // email address OR webhook URL

    private boolean enabled = true;

    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "channels")
    private Set<AlertRule> alertRules;   
}
