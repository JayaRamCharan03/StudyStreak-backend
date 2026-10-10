package com.studystreak.service;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.dto.Dtos.GoalDto;
import com.studystreak.dto.Dtos.GoalReq;
import com.studystreak.dto.Dtos.SessionDto;
import com.studystreak.dto.Dtos.SessionReq;
import com.studystreak.model.*;
import com.studystreak.repo.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Goals, check-ins, study sessions, and all the streak rules. */
@Service
public class StudyService {
    private final GoalRepository goals;
    private final CheckInRepository checkIns;
    private final SessionRepository sessions;
    private final UserRepository users;

    public StudyService(GoalRepository goals, CheckInRepository checkIns, SessionRepository sessions, UserRepository users) {
        this.goals = goals; this.checkIns = checkIns; this.sessions = sessions; this.users = users;
    }

    public static LocalDate today() { return LocalDate.now(); }

    // A daily goal is measured per day, a weekly goal per week (Monday to Sunday).
    static LocalDate periodStart(Goal.Frequency f, LocalDate date) {
        return f == Goal.Frequency.WEEKLY ? date.with(DayOfWeek.MONDAY) : date;
    }

    static LocalDate periodEnd(Goal.Frequency f, LocalDate start) {
        return f == Goal.Frequency.WEEKLY ? start.plusDays(6) : start;
    }

    static LocalDate previousPeriodStart(Goal.Frequency f, LocalDate start) {
        return start.minusDays(f == Goal.Frequency.WEEKLY ? 7 : 1);
    }

    /** The streak to show for a person. It drops to 0 if they skipped yesterday and have not studied today. */
    public int streakOf(User u) {
        if (u.lastActiveDate == null) return 0;
        return u.lastActiveDate.isBefore(today().minusDays(1)) ? 0 : u.currentStreak;
    }

    private int goalStreak(Goal g, LocalDate currentStart) {
        if (g.lastCompletedPeriod == null) return 0;
        return g.lastCompletedPeriod.isBefore(previousPeriodStart(g.frequency, currentStart)) ? 0 : g.streak;
    }

    public int progress(Goal g, LocalDate date) {
        LocalDate start = periodStart(g.frequency, date);
        LocalDate end = periodEnd(g.frequency, start);
        if (g.type == Goal.Type.MINUTES) {
            return sessions.findByGoalAndDayBetween(g, start, end).stream().mapToInt(s -> s.minutes).sum();
        }
        return checkIns.findByGoalAndDayBetween(g, start, end).stream().mapToInt(c -> c.amount).sum();
    }

    public GoalDto dto(Goal g) {
        LocalDate start = periodStart(g.frequency, today());
        int progress = progress(g, today());
        return new GoalDto(g.id, g.title, g.type.name(), g.frequency.name(), g.target, g.subject, g.resourceUrl,
            progress, progress >= g.target, goalStreak(g, start));
    }

    public List<GoalDto> list(User u) {
        return goals.findByUserOrderByIdAsc(u).stream().map(this::dto).toList();
    }

    public List<SessionDto> recentSessions(User u) {
        return sessions.findTop20ByGoalUserOrderByCreatedAtDesc(u).stream().map(this::sessionDto).toList();
    }

    private SessionDto sessionDto(StudySession s) {
        return new SessionDto(s.id, s.goal.id, s.goal.title, s.minutes, s.note, s.createdAt);
    }

    @Transactional
    public GoalDto create(User u, GoalReq r) {
        Goal g = new Goal();
        g.user = u;
        g.title = r.title().trim();
        g.type = r.type();
        g.frequency = r.frequency();
        g.target = r.target();
        g.subject = blankToNull(r.subject());
        g.resourceUrl = blankToNull(r.resourceUrl());
        goals.save(g);
        return dto(g);
    }

    @Transactional
    public void delete(User u, Long goalId) {
        Goal g = own(u, goalId);
        checkIns.deleteByGoal(g);
        sessions.deleteByGoal(g);
        goals.delete(g);
    }

    @Transactional
    public GoalDto checkIn(User u, Long goalId, Integer amount) {
        Goal g = own(u, goalId);
        if (g.type == Goal.Type.MINUTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minute goals are filled by logging a study session");
        }
        CheckIn c = new CheckIn();
        c.goal = g;
        c.day = today();
        c.amount = amount == null ? 1 : amount;
        checkIns.save(c);
        afterActivity(u, g);
        return dto(g);
    }

    @Transactional
    public SessionDto logSession(User u, SessionReq r) {
        Goal g = own(u, r.goalId());
        StudySession s = new StudySession();
        s.goal = g;
        s.day = today();
        s.minutes = r.minutes();
        s.note = blankToNull(r.note());
        s.createdAt = LocalDateTime.now();
        sessions.save(s);
        afterActivity(u, g);
        return sessionDto(s);
    }

    /** Called after every check-in or session: updates the person's streak and the goal's streak. */
    private void afterActivity(User u, Goal g) {
        LocalDate t = today();

        // personal streak: one point for each day with any activity
        if (!t.equals(u.lastActiveDate)) {
            boolean yesterday = u.lastActiveDate != null && u.lastActiveDate.equals(t.minusDays(1));
            u.currentStreak = yesterday ? u.currentStreak + 1 : 1;
            u.lastActiveDate = t;
            u.longestStreak = Math.max(u.longestStreak, u.currentStreak);
            users.save(u);
        }

        // goal streak: one point for each period the goal is completed
        LocalDate start = periodStart(g.frequency, t);
        if (progress(g, t) >= g.target && !start.equals(g.lastCompletedPeriod)) {
            LocalDate prev = previousPeriodStart(g.frequency, start);
            g.streak = prev.equals(g.lastCompletedPeriod) ? g.streak + 1 : 1;
            g.lastCompletedPeriod = start;
            goals.save(g);
        }
    }

    private Goal own(User u, Long goalId) {
        return goals.findById(goalId)
            .filter(g -> g.user.id.equals(u.id))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found"));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
        /** Sessions logged today, newest first. */
    public List<SessionDto> todaySessions(User u) {
        LocalDate t = today();
        return sessions.findByGoalUserAndDayBetween(u, t, t).stream()
            .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
            .map(this::sessionDto).toList();
    }
}