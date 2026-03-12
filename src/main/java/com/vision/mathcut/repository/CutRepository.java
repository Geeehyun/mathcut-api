package com.vision.mathcut.repository;

import com.vision.mathcut.domain.Cut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CutRepository extends JpaRepository<Cut, Long> {
    List<Cut> findByUserIdOrderByCreatedAtDesc(Long userId);
}
