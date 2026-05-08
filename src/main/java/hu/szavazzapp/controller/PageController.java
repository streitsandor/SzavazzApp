package hu.szavazzapp.controller;

import java.security.Principal;

import hu.szavazzapp.service.DatabaseConnectionTestService;
import hu.szavazzapp.service.DatabaseConnectionTestService.DatabaseStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final DatabaseConnectionTestService databaseConnectionTestService;

    public PageController(DatabaseConnectionTestService databaseConnectionTestService) {
        this.databaseConnectionTestService = databaseConnectionTestService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/main";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/main")
    public String main(Model model, Principal principal) {
        DatabaseStatus databaseStatus = databaseConnectionTestService.testConnection();

        model.addAttribute("databaseStatus", databaseStatus);
        model.addAttribute("username", principal != null ? principal.getName() : "ismeretlen");

        return "main";
    }
}
