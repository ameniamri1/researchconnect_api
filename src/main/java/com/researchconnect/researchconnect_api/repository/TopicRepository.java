package com.researchconnect.researchconnect_api.repository;

import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByTeacher(User teacher);

    Page<Topic> findByTeacher(User teacher, Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE " +
            "(:category IS NULL OR t.category = :category) AND " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Topic> findByFilters(String category, String keyword, Pageable pageable);
}
