package hu.szavazzapp.controller;

import java.security.Principal;

import hu.szavazzapp.service.DatabaseConnectionTestService;
import hu.szavazzapp.service.DatabaseConnectionTestService.DatabaseStatus;
import hu.szavazzapp.service.PollQueryService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final DatabaseConnectionTestService databaseConnectionTestService;
    private final PollQueryService pollQueryService;

    public PageController(
            DatabaseConnectionTestService databaseConnectionTestService,
            PollQueryService pollQueryService) {
        this.databaseConnectionTestService = databaseConnectionTestService;
        this.pollQueryService = pollQueryService;
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
        String username = principal != null ? principal.getName() : "ismeretlen";
        DatabaseStatus databaseStatus = databaseConnectionTestService.testConnection();

        model.addAttribute("username", username);
        model.addAttribute("databaseStatus", databaseStatus);

        model.addAttribute("topPolls", pollQueryService.findTopPolls(10));
        model.addAttribute("otherUserPolls", pollQueryService.findOtherUserPolls(username));
        model.addAttribute("ownPolls", pollQueryService.findOwnPolls(username));

        return "main";
    }
}