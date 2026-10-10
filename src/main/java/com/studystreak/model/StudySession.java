package com.studystreak.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class StudySession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) public Goal goal;
    public LocalDate day;
    public int minutes;
    @Column(length = 500) public String note;
    public LocalDateTime createdAt;
}