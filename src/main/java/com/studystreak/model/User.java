package com.studystreak.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    @Column(unique = true, nullable = false) public String email;
    public String passwordHash;
    public int currentStreak;
    public int longestStreak;
    public LocalDate lastActiveDate;
}
