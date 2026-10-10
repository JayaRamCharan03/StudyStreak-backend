package com.studystreak.model;

import jakarta.persistence.*;

@Entity
public class StudyGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    @Column(unique = true) public String inviteCode;
    @ManyToOne public User owner;
}
