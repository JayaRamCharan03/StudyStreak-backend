package com.studystreak.repo;

import com.studystreak.model.StudyGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<StudyGroup, Long> {

    Optional<StudyGroup> findByInviteCode(String inviteCode);
}