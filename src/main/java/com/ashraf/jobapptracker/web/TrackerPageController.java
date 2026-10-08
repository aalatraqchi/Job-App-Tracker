package com.ashraf.jobapptracker.web;

import com.ashraf.jobapptracker.application.ApplicationNotFoundException;
import com.ashraf.jobapptracker.application.ApplicationService;
import com.ashraf.jobapptracker.application.ApplicationStatus;
import com.ashraf.jobapptracker.application.CreateApplicationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.time.LocalDate;

@Controller
public class TrackerPageController {
    private final ApplicationService service;

    public TrackerPageController(ApplicationService service) {
        this.service = service;
    }

    @GetMapping({"/", "/tracker"})
    public String list(Model model) {
        var applications = service.findAll();
        model.addAttribute("applications", applications);
        model.addAttribute("interviews", applications.stream()
                .filter(application -> application.getStatus() == ApplicationStatus.INTERVIEW).count());
        model.addAttribute("offers", applications.stream()
                .filter(application -> application.getStatus() == ApplicationStatus.OFFER).count());
        return "tracker/list";
    }

    @ModelAttribute("statuses")
    public ApplicationStatus[] statuses() {
        return ApplicationStatus.values();
    }

    @GetMapping("/tracker/new")
    public String newApplication(Model model) {
        model.addAttribute("form", new ApplicationForm("", "", LocalDate.now(), ApplicationStatus.APPLIED, "", ""));
        return "tracker/form";
    }

    @PostMapping("/tracker")
    public String create(@Valid @ModelAttribute("form") ApplicationForm form,
                         BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "tracker/form";
        }
        var application = service.create(new CreateApplicationRequest(
                form.company().strip(), form.position().strip(), form.appliedDate(),
                form.status(), form.jobUrl(), form.notes()
        ));
        redirect.addFlashAttribute("success", "Application added. You're one step closer.");
        return "redirect:/tracker/" + application.getId();
    }

    @GetMapping("/tracker/{id}")
    public String detail(@PathVariable("id") long id, Model model) {
        var application = service.findById(id);
        model.addAttribute("job", application);
        model.addAttribute("safeJobUrl", safeJobUrl(application.getJobUrl()));
        return "tracker/detail";
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound() {
        return "tracker/not-found";
    }

    private String safeJobUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(url);
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null ? url : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
