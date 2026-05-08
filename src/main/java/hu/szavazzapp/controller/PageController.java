package hu.szavazzapp.controller;

import java.security.Principal;
import java.util.List;

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

        model.addAttribute("topPolls", getTopPollPlaceholders());
        model.addAttribute("otherUserPolls", getOtherUserPolls());
        model.addAttribute("ownPolls", getOwnPolls());

        return "main";
    }

    private List<PollView> getTopPollPlaceholders() {
        return List.of(
                new PollView(
                        1L,
                        "Top 1 placeholder",
                        List.of("Technológia", "Közösség"),
                        "Lorem ipsum dolor sit amet consectetur, adipisicing elit. Voluptatum accusamus qui perferendis. Animi nisi consequuntur ab reiciendis cumque! Recusandae corporis temporibus tenetur dolorem dolorum est.",
                        List.of("Igen", "Nem", "Talán"),
                        "Rendszer",
                        128),
                new PollView(
                        2L,
                        "Top 2 placeholder",
                        List.of("Oktatás"),
                        "Lorem ipsum dolor sit amet consectetur adipisicing elit. Ab, porro!",
                        List.of("Hasznos", "Nem hasznos"),
                        "Rendszer",
                        94));
    }

    private List<PollView> getOtherUserPolls() {
        return List.of(
                new PollView(
                        101L,
                        "All Title 1",
                        List.of("Fejlesztés", "Backend", "Frontend"),
                        "Lorem ipsum dolor, sit amet consectetur adipisicing elit. Laudantium quisquam ratione hic, dignissimos debitis quibusdam provident nostrum aliquam similique alias?",
                        List.of("Admin felület", "Szavazás létrehozása", "Eredménydiagramok", "Felhasználói profil"),
                        "anna",
                        35),
                new PollView(
                        102L,
                        "All Title 2",
                        List.of("Közösség", "Vélemény"),
                        "Lorem ipsum dolor sit amet.",
                        List.of("Technológia", "Sport", "Oktatás", "Szórakozás"),
                        "peter",
                        18),
                new PollView(
                        103L,
                        "All Title 3",
                        List.of("Rendszer", "Szabályok"),
                        "Lorem ipsum dolor sit amet consectetur adipisicing.",
                        List.of("Igen, mindig", "Nem szükséges", "Legyen opcionális"),
                        "eszter",
                        51));
    }

    private List<PollView> getOwnPolls() {
        return List.of(
                new PollView(
                        201L,
                        "Own Title 1",
                        List.of("Teszt", "Saját"),
                        "Lorem ipsum dolor sit amet.",
                        List.of("Első opció", "Második opció", "Harmadik opció"),
                        "user",
                        7),
                new PollView(
                        202L,
                        "Own Title 2",
                        List.of("Fejlesztés"),
                        "Lorem ipsum dolor sit amet consectetur adipisicing elit. Odio quam, quos tempora assumenda nostrum delectus.",
                        List.of("Megjelenítés", "Szerkesztés", "Törlés"),
                        "user",
                        3));
    }

    public record PollView(
            Long id,
            String title,
            List<String> topics,
            String description,
            List<String> options,
            String ownerName,
            int voteCount) {
    }
}