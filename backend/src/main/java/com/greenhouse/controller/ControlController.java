package com.greenhouse.controller;

import com.greenhouse.dto.ControlCommandRequest;
import com.greenhouse.entity.ControlCommand;
import com.greenhouse.service.ControlService;
import javax.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control")
public class ControlController {

  private final ControlService controlService;

  public ControlController(ControlService controlService) {
    this.controlService = controlService;
  }

  @PostMapping("/command")
  public ControlCommand sendCommand(@Valid @RequestBody ControlCommandRequest request) {
    return controlService.sendCommand(request);
  }

  @GetMapping("/commands")
  public List<ControlCommand> latestCommands() {
    return controlService.latestCommands();
  }
}
