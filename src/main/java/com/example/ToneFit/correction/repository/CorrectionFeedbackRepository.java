package com.example.ToneFit.correction.repository;

import com.example.ToneFit.correction.model.CorrectionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CorrectionFeedbackRepository extends JpaRepository<CorrectionFeedback, Long> {

    List<CorrectionFeedback> findBySessionIdOrderByIndexAsc(Long sessionId);

    Optional<CorrectionFeedback> findBySessionIdAndIndex(Long sessionId, int index);

    void deleteBySessionId(Long sessionId);
}
