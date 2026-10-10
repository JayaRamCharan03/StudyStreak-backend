package com.studystreak.service;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.model.*;
import com.studystreak.repo.*;
import java.time.*;
import java.util.*;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final GoalRepository goals;
    private final SessionRepository sessions;
    private final CheckInRepository checkIns;
    private final MemberRepository members;
    private final StudyService study;

    public DashboardService(GoalRepository goals, SessionRepository sessions, CheckInRepository checkIns, MemberRepository members, StudyService study) {
        this.goals = goals; this.sessions = sessions; this.checkIns = checkIns; this.members = members; this.study = study;
    }

    public DashboardDto build(User u) {
        LocalDate t = StudyService.today(), mon = t.with(DayOfWeek.MONDAY);
        List<StudySession> ws = sessions.findByGoalUserAndDayBetween(u, mon, mon.plusDays(6));
        List<CheckIn> wc = checkIns.findByGoalUserAndDayBetween(u, mon, mon.plusDays(6));
        int minutes = ws.stream().mapToInt(s -> s.minutes).sum();   // fixed: s.minutes, not s.minutes()
        List<Goal> daily = goals.findByUserOrderByIdAsc(u).stream().filter(g -> g.frequency == Goal.Frequency.DAILY).toList();

        List<Integer> weekly = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = mon.plusDays(i);
            int n = 0;
            for (Goal g : daily) {
                int p = g.type == Goal.Type.MINUTES
                    ? ws.stream().filter(s -> s.goal.id.equals(g.id) && s.day.equals(d)).mapToInt(s -> s.minutes).sum()
                    : wc.stream().filter(c -> c.goal.id.equals(g.id) && c.day.equals(d)).mapToInt(c -> c.amount).sum();
                if (p >= g.target) n++;
            }
            weekly.add(n);
        }

        LocalDate from = t.minusDays(97);
        Map<LocalDate, Integer> m = new HashMap<>();
        sessions.findByGoalUserAndDayBetween(u, from, t).forEach(s -> m.merge(s.day, 1, Integer::sum));
        checkIns.findByGoalUserAndDayBetween(u, from, t).forEach(c -> m.merge(c.day, 1, Integer::sum));
        List<Integer> activity = IntStream.range(0, 98).mapToObj(i -> m.getOrDefault(from.plusDays(i), 0)).toList();

        Integer rank = null; String groupName = null; List<SnapshotDto> snap = List.of();
        List<GroupMember> mine = members.findByUser(u);
        if (!mine.isEmpty()) {
            StudyGroup g = mine.get(0).group;
            groupName = g.name;
            List<User> sorted = members.findByGroup(g).stream().map(x -> x.user)
                .sorted((a, b) -> Integer.compare(study.streakOf(b), study.streakOf(a))).toList();
            for (int i = 0; i < sorted.size(); i++) if (sorted.get(i).id.equals(u.id)) rank = i + 1;
            snap = sorted.stream().limit(3).map(x -> new SnapshotDto(x.name, study.streakOf(x))).toList();
        }

        List<GoalDto> today = daily.stream().map(study::dto).toList();
        return new DashboardDto(u.name, study.streakOf(u), u.longestStreak, minutes, rank, groupName, weekly, activity, today, snap);
    }
}