package com.ashraf.jobapptracker.web;

import com.ashraf.jobapptracker.application.ApplicationNotFoundException;
import com.ashraf.jobapptracker.application.ApplicationService;
import com.ashraf.jobapptracker.application.ApplicationStatus;
import com.ashraf.jobapptracker.application.CreateApplicationRequest;
import com.ashraf.jobapptracker.application.UpdateApplicationRequest;
import com.ashraf.jobapptracker.interview.InterviewService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.time.LocalDate;

@Controller
public class TrackerPageController {
    private final ApplicationService service;
    private final InterviewService interviews;

    public TrackerPageController(ApplicationService service, InterviewService interviews) {
        this.service = service;
        this.interviews = interviews;
    }

    @GetMapping({"/", "/tracker"})
    public String list(@RequestParam(name = "status", required = false) ApplicationStatus status,
                       @RequestParam(name = "company", required = false) String company, Model model) {
        var allApplications = service.findAll();
        var applications = service.findAll(status, company);
        model.addAttribute("applications", applications);
        model.addAttribute("total", allApplications.size());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("companyFilter", company == null ? "" : company.strip());
        model.addAttribute("hasFilters", status != null || (company != null && !company.isBlank()));
        model.addAttribute("interviews", allApplications.stream()
                .filter(application -> application.getStatus() == ApplicationStatus.INTERVIEW).count());
        model.addAttribute("offers", allApplications.stream()
                .filter(application -> application.getStatus() == ApplicationStatus.OFFER).count());
        return "tracker/list";
    }

    @ModelAttribute("statuses")
    public ApplicationStatus[] statuses() {
        return ApplicationStatus.values();
    }

    @GetMapping("/tracker/new")
    public String newApplication(Model model) {
        prepareForm(model, null);
        model.addAttribute("form", new ApplicationForm("", "", LocalDate.now(), ApplicationStatus.APPLIED, "", ""));
        return "tracker/form";
    }

    @PostMapping("/tracker")
    public String create(@Valid @ModelAttribute("form") ApplicationForm form,
                         BindingResult errors, RedirectAttributes redirect, Model model) {
        if (errors.hasErrors()) {
            prepareForm(model, null);
            return "tracker/form";
        }
        var application = service.create(new CreateApplicationRequest(
                form.company().strip(), form.position().strip(), form.appliedDate(),
                form.status(), form.jobUrl(), form.notes()
        ));
        redirect.addFlashAttribute("success", "Application added. You're one step closer.");
        return "redirect:/tracker/" + application.getId();
    }

    @GetMapping("/tracker/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        var application = service.findById(id);
        prepareForm(model, id);
        model.addAttribute("form", new ApplicationForm(
                application.getCompany(), application.getPosition(), application.getAppliedDate(),
                application.getStatus(), application.getJobUrl(), application.getNotes()));
        return "tracker/form";
    }

    @PostMapping("/tracker/{id}/edit")
    public String update(@PathVariable("id") long id,
                         @Valid @ModelAttribute("form") ApplicationForm form,
                         BindingResult errors, RedirectAttributes redirect, Model model) {
        service.findById(id);
        prepareForm(model, id);
        if (errors.hasErrors()) {
            return "tracker/form";
        }
        service.update(id, new UpdateApplicationRequest(
                form.company().strip(), form.position().strip(), form.appliedDate(),
                form.status(), form.jobUrl(), form.notes()));
        redirect.addFlashAttribute("success", "Application updated.");
        return "redirect:/tracker/" + id;
    }

    @GetMapping("/tracker/{id}/delete")
    public String confirmDelete(@PathVariable("id") long id, Model model) {
        model.addAttribute("job", service.findById(id));
        return "tracker/delete";
    }

    @PostMapping("/tracker/{id}/delete")
    public String delete(@PathVariable("id") long id,
                         @RequestParam(name = "confirmed", defaultValue = "false") boolean confirmed,
                         RedirectAttributes redirect) {
        service.findById(id);
        if (!confirmed) {
            return "redirect:/tracker/" + id + "/delete";
        }
        service.delete(id);
        redirect.addFlashAttribute("success", "Application deleted.");
        return "redirect:/tracker";
    }

    private void prepareForm(Model model, Long id) {
        model.addAttribute("editing", id != null);
        model.addAttribute("formAction", id == null ? "/tracker" : "/tracker/" + id + "/edit");
        model.addAttribute("cancelUrl", id == null ? "/tracker" : "/tracker/" + id);
    }

    @GetMapping("/tracker/{id}")
    public String detail(@PathVariable("id") long id, Model model) {
        var application = service.findById(id);
        model.addAttribute("job", application);
        model.addAttribute("safeJobUrl", safeJobUrl(application.getJobUrl()));
        model.addAttribute("stages", interviews.findAll(id));
        return "tracker/detail";
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound() {
        return "tracker/not-found";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidId(MethodArgumentTypeMismatchException exception, Model model) {
        if ("status".equals(exception.getName())) {
            model.addAttribute("errorTitle", "Invalid status filter");
            model.addAttribute("errorMessage", "Choose a status from the filter menu to continue.");
        }
        return "tracker/bad-request";
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
