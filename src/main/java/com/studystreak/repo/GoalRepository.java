package com.studystreak.repo;

import com.studystreak.model.Goal;
import com.studystreak.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserOrderByIdAsc(User user);
}