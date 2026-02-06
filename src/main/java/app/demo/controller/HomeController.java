package app.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  private final Environment environment;

  public HomeController(Environment environment) {
    this.environment = environment;
  }

  @GetMapping({"/", "/home"})
  public String home(Model m) {
    logger.info("home page requested.");
    m.addAttribute("profile", String.join(", ", environment.getActiveProfiles()));
    return "home";
  }

  @GetMapping("/triggerError")
  public String triggerError() {
    logger.error("This is a test error message.");
    throw new RuntimeException("This is a test exception.");
  }
}
