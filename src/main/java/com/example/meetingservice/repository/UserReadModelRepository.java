package com.example.meetingservice.repository;

import com.example.meetingservice.entity.UserReadModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserReadModelRepository extends JpaRepository<UserReadModelEntity, UUID> {
}
