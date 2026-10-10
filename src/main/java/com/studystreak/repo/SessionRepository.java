package com.studystreak.repo;

import com.studystreak.model.Goal;
import com.studystreak.model.StudySession;
import com.studystreak.model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByGoalUserAndDayBetween(User user, LocalDate start, LocalDate end);

    List<StudySession> findByGoalAndDayBetween(Goal goal, LocalDate start, LocalDate end);

    List<StudySession> findTop10ByGoalUserInOrderByCreatedAtDesc(List<User> users);

    List<StudySession> findTop20ByGoalUserOrderByCreatedAtDesc(User user);

    void deleteByGoal(Goal goal);
}