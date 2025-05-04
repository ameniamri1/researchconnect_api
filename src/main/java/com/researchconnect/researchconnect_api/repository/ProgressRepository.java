package com.researchconnect.researchconnect_api.repository;

import com.researchconnect.researchconnect_api.entity.Progress;
import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByStudent(User student);

    List<Progress> findByTopic(Topic topic);

    Optional<Progress> findByTopicAndStudent(Topic topic, User student);
}
