package com.wexa.skillpath.controller;

import com.wexa.skillpath.model.Option;
import com.wexa.skillpath.model.Recommendation;
import com.wexa.skillpath.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class WebController {
    private final GraphService service;

    public WebController(GraphService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("learners", service.learners());
            model.addAttribute("roles", service.roles());
            model.addAttribute("connected", true);
        } catch (Exception ex) {
            model.addAttribute("learners", List.of());
            model.addAttribute("roles", List.of());
            model.addAttribute("connected", false);
            model.addAttribute("error", friendlyMessage(ex));
        }
        return "index";
    }

    @GetMapping("/recommendations")
    public String recommendations(
            @RequestParam String learnerId,
            @RequestParam String roleId,
            Model model) {
        try {
            List<Recommendation> recommendations = service.recommendations(learnerId, roleId);
            int[] coverage = service.coverage(learnerId, roleId);
            model.addAttribute("recommendations", recommendations);
            model.addAttribute("coverage", coverage);
            model.addAttribute("learnerId", learnerId);
            model.addAttribute("roleId", roleId);
            model.addAttribute("connected", true);
        } catch (Exception ex) {
            model.addAttribute("recommendations", List.of());
            model.addAttribute("coverage", new int[]{0, 0});
            model.addAttribute("connected", false);
            model.addAttribute("error", friendlyMessage(ex));
        }
        try {
            model.addAttribute("learners", service.learners());
            model.addAttribute("roles", service.roles());
        } catch (Exception ignored) {
            model.addAttribute("learners", List.of());
            model.addAttribute("roles", List.of());
        }
        return "index";
    }

    @GetMapping("/api/paths")
    @ResponseBody
    public ResponseEntity<?> learningPaths(@RequestParam String learnerId, @RequestParam String roleId) {
        try {
            return ResponseEntity.ok(service.learningPaths(learnerId, roleId));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", friendlyMessage(ex)));
        }
    }

    @PostMapping("/admin/seed")
    @ResponseBody
    public ResponseEntity<String> seed() {
        try {
            service.seed();
            return ResponseEntity.ok("Seed data loaded successfully.");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(friendlyMessage(ex));
        }
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() {
        try {
            service.verifyConnectivity();
            return ResponseEntity.ok("UP");
        } catch (Exception ex) {
            return ResponseEntity.status(503).body("DOWN: " + friendlyMessage(ex));
        }
    }

    private String friendlyMessage(Exception ex) {
        return "Graph database is unavailable. Check COGNODB_URI, COGNODB_USERNAME, COGNODB_PASSWORD and network access.";
    }
}
