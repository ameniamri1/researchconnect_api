package com.researchconnect.researchconnect_api.repository;

import com.researchconnect.researchconnect_api.entity.Resource;
import com.researchconnect.researchconnect_api.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByTopic(Topic topic);
}
