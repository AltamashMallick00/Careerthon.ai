package com.careerthon.repository;

import com.careerthon.model.LmsNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LmsNotificationRepository extends JpaRepository<LmsNotification, Long> {
    List<LmsNotification> findByTargetUserIdOrTargetUserIsNullOrderByCreatedAtDesc(Long userId);
    List<LmsNotification> findByTargetUserIsNullOrderByCreatedAtDesc();
}
