package com.greenhouse.repository;

import com.greenhouse.entity.ControlRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlRuleRepository extends JpaRepository<ControlRule, Long> {
  List<ControlRule> findByEnabledTrueOrderByIdAsc();
}
