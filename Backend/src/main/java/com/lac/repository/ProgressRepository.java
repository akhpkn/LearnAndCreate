package com.lac.repository;

import com.lac.model.Progress;
import com.lac.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Progress findByUser(User user);

    @Query("select p from Progress p where p.user.userId=:userId")
    Progress findByUserId(@Param("userId") Long userId);
}
