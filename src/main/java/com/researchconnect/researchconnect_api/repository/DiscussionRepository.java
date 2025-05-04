package com.researchconnect.researchconnect_api.repository;

import com.researchconnect.researchconnect_api.entity.Discussion;
import com.researchconnect.researchconnect_api.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    List<Discussion> findByTopicOrderByCreatedAtDesc(Topic topic);
}