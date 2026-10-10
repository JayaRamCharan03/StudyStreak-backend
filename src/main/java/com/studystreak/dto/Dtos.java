package com.studystreak.dto;

import com.studystreak.model.Goal;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class Dtos {
    public record RegisterReq(@NotBlank String name, @NotBlank @Email String email, @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password) {}
    public record LoginReq(@NotBlank String email, @NotBlank String password) {}
    public record UserDto(Long id, String name, String email, int currentStreak, int longestStreak) {}
    public record AuthRes(String token, UserDto user) {}

    public record GoalReq(@NotBlank String title, @NotNull Goal.Type type, @NotNull Goal.Frequency frequency, @Min(1) int target, String subject, String resourceUrl) {}
    public record GoalDto(Long id, String title, String type, String frequency, int target, String subject, String resourceUrl, int progress, boolean done, int streak) {}
    public record CheckInReq(@Min(1) Integer amount) {}

    public record SessionReq(@NotNull Long goalId, @Min(1) int minutes, @Size(max = 500) String note) {}
    public record SessionDto(Long id, Long goalId, String goalTitle, int minutes, String note, LocalDateTime createdAt) {}

    public record GroupReq(@NotBlank String name) {}
    public record JoinReq(@NotBlank String code) {}
    public record GroupDto(Long id, String name, int memberCount, int topStreak, String inviteCode) {}
    public record MemberDto(int rank, Long userId, String name, int streak, int weekDays, String status, boolean me) {}
    public record ActivityDto(String name, String text, LocalDateTime at) {}
    public record GroupDetailDto(Long id, String name, String inviteCode, List<MemberDto> members, List<ActivityDto> activity) {}

    public record LeaderDto(int rank, Long userId, String name, int streak, int longest, boolean me) {}
    public record SnapshotDto(String name, int streak) {}
    public record DashboardDto(String name, int currentStreak, int longestStreak, int minutesThisWeek, Integer groupRank, String groupName,
                               List<Integer> weekly, List<Integer> activity, List<GoalDto> todayGoals, List<SnapshotDto> groupSnapshot) {}
}
