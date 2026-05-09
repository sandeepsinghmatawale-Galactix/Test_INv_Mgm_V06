package com.barinventory.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.barinventory.entities.UserBarAccess;

@Repository
public interface UserBarAccessRepository extends JpaRepository<UserBarAccess, Long> {

    @Query("""
            select uba
            from UserBarAccess uba
            join fetch uba.bar b
            where uba.user.id = :userId
              and uba.active = true
            order by b.barName asc
            """)
    List<UserBarAccess> findActiveWithBarByUserId(@Param("userId") Long userId);
}

