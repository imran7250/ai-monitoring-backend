package com.imran.aimonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imran.aimonitoring.entity.NotificationChannel;

public interface NotificationChannelRepository
        extends JpaRepository<NotificationChannel, Long> {
}
