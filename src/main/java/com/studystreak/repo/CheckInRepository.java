package com.studystreak.repo;

import com.studystreak.model.CheckIn;
import com.studystreak.model.Goal;
import com.studystreak.model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByGoalUserAndDayBetween(User user, LocalDate start, LocalDate end);

    List<CheckIn> findByGoalAndDayBetween(Goal goal, LocalDate start, LocalDate end);

    void deleteByGoal(Goal goal);
}