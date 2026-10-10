package com.studystreak.service;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.model.*;
import com.studystreak.repo.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Accountability groups and the leaderboard. */
@Service
public class GroupService {
    private final GroupRepository groups;
    private final MemberRepository members;
    private final UserRepository users;
    private final SessionRepository sessions;
    private final CheckInRepository checkIns;
    private final StudyService study;

    public GroupService(GroupRepository groups, MemberRepository members, UserRepository users, SessionRepository sessions, CheckInRepository checkIns, StudyService study) {
        this.groups = groups; this.members = members; this.users = users; this.sessions = sessions; this.checkIns = checkIns; this.study = study;
    }

    public List<GroupDto> mine(User u) { return members.findByUser(u).stream().map(m -> toDto(m.group)).toList(); }

    GroupDto toDto(StudyGroup g) {
        List<GroupMember> ms = members.findByGroup(g);
        int top = ms.stream().mapToInt(m -> study.streakOf(m.user)).max().orElse(0);
        return new GroupDto(g.id, g.name, ms.size(), top, g.inviteCode);
    }

    @Transactional
    public GroupDto create(User u, String name) {
        StudyGroup g = new StudyGroup();
        g.name = name.trim(); g.owner = u; g.inviteCode = newCode();
        groups.save(g);
        add(g, u);
        return toDto(g);
    }

    @Transactional
    public GroupDto join(User u, String code) {
        StudyGroup g = groups.findByInviteCode(code.trim().toUpperCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite code not found"));
        if (!members.existsByGroupAndUser(g, u)) add(g, u);
        return toDto(g);
    }

    public GroupDetailDto detail(User u, Long id) {
        StudyGroup g = groups.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        if (!members.existsByGroupAndUser(g, u)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not in this group");
        List<User> us = members.findByGroup(g).stream().map(m -> m.user)
            .sorted((a, b) -> Integer.compare(study.streakOf(b), study.streakOf(a))).toList();
        List<MemberDto> out = new ArrayList<>();
        for (int i = 0; i < us.size(); i++) {
            User m = us.get(i);
            out.add(new MemberDto(i + 1, m.id, m.name, study.streakOf(m), weekDays(m), status(m), m.id.equals(u.id)));
        }
        List<ActivityDto> act = sessions.findTop10ByGoalUserInOrderByCreatedAtDesc(us).stream().limit(5)
            .map(s -> new ActivityDto(s.goal.user.name, "finished " + s.goal.title + " (" + s.minutes + " min)", s.createdAt)).toList();
        return new GroupDetailDto(g.id, g.name, g.inviteCode, out, act);
    }

    public List<LeaderDto> leaderboard(User u, String scope) {
        Collection<User> pool;
        if ("groups".equals(scope)) {
            Map<Long, User> m = new LinkedHashMap<>();
            m.put(u.id, u);
            for (GroupMember gm : members.findByUser(u))
                for (GroupMember x : members.findByGroup(gm.group)) m.put(x.user.id, x.user);
            pool = m.values();
        } else pool = users.findAll();
        List<User> sorted = pool.stream().sorted((a, b) -> {
            int c = Integer.compare(study.streakOf(b), study.streakOf(a));
            return c != 0 ? c : Integer.compare(b.longestStreak, a.longestStreak);
        }).limit(20).toList();
        List<LeaderDto> out = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            User x = sorted.get(i);
            out.add(new LeaderDto(i + 1, x.id, x.name, study.streakOf(x), x.longestStreak, x.id.equals(u.id)));
        }
        return out;
    }

    int weekDays(User u) {
        LocalDate t = StudyService.today(), mon = t.with(java.time.DayOfWeek.MONDAY);
        Set<LocalDate> days = new HashSet<>();
        sessions.findByGoalUserAndDayBetween(u, mon, t).forEach(s -> days.add(s.day));
        checkIns.findByGoalUserAndDayBetween(u, mon, t).forEach(c -> days.add(c.day));
        return days.size();
    }

    String status(User u) {
        List<GoalDto> daily = study.list(u).stream().filter(g -> g.frequency().equals("DAILY")).toList();
        if (daily.isEmpty()) return study.streakOf(u) == 0 ? "Missed" : "Pending";
        long done = daily.stream().filter(GoalDto::done).count();
        if (done == daily.size()) return "Done";
        if (done > 0) return done + " of " + daily.size();
        return u.lastActiveDate != null && u.lastActiveDate.equals(StudyService.today()) ? "Pending"
            : (study.streakOf(u) == 0 ? "Missed" : "Pending");
    }

    private void add(StudyGroup g, User u) {
        GroupMember m = new GroupMember(); m.group = g; m.user = u; members.save(m);
    }

    private String newCode() {
        String c;
        do { c = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); } while (groups.findByInviteCode(c).isPresent());
        return c;
    }
}
