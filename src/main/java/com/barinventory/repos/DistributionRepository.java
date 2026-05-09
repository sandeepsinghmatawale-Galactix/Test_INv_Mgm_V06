package com.barinventory.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.barinventory.entities.Distribution;

import jakarta.persistence.LockModeType;

@Repository
public interface DistributionRepository 
        extends JpaRepository<Distribution, Long> {
	
	 

    Optional<Distribution> findBySessionSessionId(Long sessionId);

    Optional<Distribution> findTopByOrderByDistributionIdDesc();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Distribution d where d.distributionId = :id")
    Optional<Distribution> findByIdForUpdate(@Param("id") Long id);
	 
	 
	 
}
