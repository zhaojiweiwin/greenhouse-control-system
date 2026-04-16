package com.greenhouse.repository;

import com.greenhouse.entity.ControlCommand;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlCommandRepository extends JpaRepository<ControlCommand, Long> {
  List<ControlCommand> findTop20ByOrderBySentTimeDesc();
}
