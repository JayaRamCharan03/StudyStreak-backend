package com.studystreak.repo;

import com.studystreak.model.GroupMember;
import com.studystreak.model.StudyGroup;
import com.studystreak.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUser(User user);

    List<GroupMember> findByGroup(StudyGroup group);

    boolean existsByGroupAndUser(StudyGroup group, User user);
}