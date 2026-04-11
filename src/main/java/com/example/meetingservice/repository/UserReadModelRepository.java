package com.example.meetingservice.repository;

import com.example.meetingservice.entity.UserReadModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReadModelRepository extends JpaRepository<UserReadModelEntity, Long> {
}
