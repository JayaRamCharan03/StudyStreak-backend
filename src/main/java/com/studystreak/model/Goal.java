package com.studystreak.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Goal {
    public enum Type { COUNT, MINUTES, CHECKLIST }
    public enum Frequency { DAILY, WEEKLY }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) public User user;
    public String title;
    @Enumerated(EnumType.STRING) public Type type;
    @Enumerated(EnumType.STRING) public Frequency frequency;
    public int target;
    public String subject;
    public String resourceUrl;
    public int streak;
    public LocalDate lastCompletedPeriod;
}
