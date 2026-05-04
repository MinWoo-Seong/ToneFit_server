package com.example.ToneFit.prompt.repository;

import com.example.ToneFit.prompt.model.PromptPurpose;
import com.example.ToneFit.prompt.model.PromptVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromptVersionRepository extends JpaRepository<PromptVersion, Long> {

    Optional<PromptVersion> findFirstByPurposeAndIsActiveTrueOrderByCreatedAtDesc(PromptPurpose purpose);
}
