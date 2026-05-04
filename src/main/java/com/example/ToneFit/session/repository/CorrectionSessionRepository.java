package com.example.ToneFit.session.repository;

import com.example.ToneFit.session.model.CorrectionSession;
import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Receiver;
import com.example.ToneFit.session.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface CorrectionSessionRepository extends JpaRepository<CorrectionSession, Long> {

    Optional<CorrectionSession> findByUserIdAndStatus(Long userId, Status status);

    @Query("""
            select s from CorrectionSession s
            where s.user.id = :userId and s.status in :statuses
            order by s.createdAt desc
            """)
    java.util.List<CorrectionSession> findByUserIdAndStatusIn(@Param("userId") Long userId,
                                                             @Param("statuses") Collection<Status> statuses);

    @Query("""
            select s from CorrectionSession s
            where s.user.id = :userId and s.status = :status
              and (:receiverType is null or s.receiverType = :receiverType)
              and (:purpose is null or s.purpose = :purpose)
            """)
    Page<CorrectionSession> searchHistory(@Param("userId") Long userId,
                                          @Param("status") Status status,
                                          @Param("receiverType") Receiver receiverType,
                                          @Param("purpose") Purpose purpose,
                                          Pageable pageable);
}
