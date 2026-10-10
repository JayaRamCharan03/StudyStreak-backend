package com.studystreak.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class CheckIn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) public Goal goal;
    public LocalDate day;
    public int amount;
}