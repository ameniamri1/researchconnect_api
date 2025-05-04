package com.researchconnect.researchconnect_api.repository;

import com.researchconnect.researchconnect_api.entity.Application;
import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByTopic(Topic topic);

    List<Application> findByStudent(User student);

    Optional<Application> findByTopicAndStudent(Topic topic, User student);
}
