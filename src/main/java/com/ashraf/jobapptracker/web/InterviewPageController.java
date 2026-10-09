package com.ashraf.jobapptracker.web;

import com.ashraf.jobapptracker.application.ApplicationService;
import com.ashraf.jobapptracker.application.ApplicationNotFoundException;
import com.ashraf.jobapptracker.interview.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tracker/{applicationId}/interviews")
public class InterviewPageController {
    private final ApplicationService applications;
    private final InterviewService interviews;

    public InterviewPageController(ApplicationService applications, InterviewService interviews) {
        this.applications = applications;
        this.interviews = interviews;
    }

    @ModelAttribute("outcomes")
    public InterviewOutcome[] outcomes() { return InterviewOutcome.values(); }

    @GetMapping("/new")
    public String newStage(@PathVariable("applicationId") long applicationId, Model model) {
        prepareForm(applicationId, null, model);
        model.addAttribute("form", new InterviewForm("", null, "+00:00", InterviewOutcome.PENDING, ""));
        return "tracker/interview-form";
    }

    @PostMapping
    public String create(@PathVariable("applicationId") long applicationId,
            @Valid @ModelAttribute("form") InterviewForm form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        prepareForm(applicationId, null, model);
        if (errors.hasErrors()) { return "tracker/interview-form"; }
        interviews.create(applicationId, new CreateInterviewRequest(form.stageName(), form.scheduledTime(),
                form.outcome(), form.notes()));
        redirect.addFlashAttribute("success", "Interview stage added.");
        return "redirect:/tracker/" + applicationId;
    }

    @GetMapping("/{interviewId}/edit")
    public String edit(@PathVariable("applicationId") long applicationId,
            @PathVariable("interviewId") long interviewId, Model model) {
        var stage = interviews.findById(applicationId, interviewId);
        var time = stage.getScheduledAt();
        prepareForm(applicationId, interviewId, model);
        model.addAttribute("form", new InterviewForm(stage.getStageName(),
                time == null ? null : time.toLocalDateTime(),
                time == null ? "+00:00" : time.getOffset().getId(), stage.getOutcome(), stage.getNotes()));
        return "tracker/interview-form";
    }

    @PostMapping("/{interviewId}/edit")
    public String update(@PathVariable("applicationId") long applicationId,
            @PathVariable("interviewId") long interviewId,
            @Valid @ModelAttribute("form") InterviewForm form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        interviews.findById(applicationId, interviewId);
        prepareForm(applicationId, interviewId, model);
        if (errors.hasErrors()) { return "tracker/interview-form"; }
        interviews.update(applicationId, interviewId, new UpdateInterviewRequest(form.stageName(),
                form.scheduledTime(), form.outcome(), form.notes()));
        redirect.addFlashAttribute("success", "Interview stage updated.");
        return "redirect:/tracker/" + applicationId;
    }

    @GetMapping("/{interviewId}/delete")
    public String confirmDelete(@PathVariable("applicationId") long applicationId,
            @PathVariable("interviewId") long interviewId, Model model) {
        model.addAttribute("job", applications.findById(applicationId));
        model.addAttribute("stage", interviews.findById(applicationId, interviewId));
        return "tracker/interview-delete";
    }

    @PostMapping("/{interviewId}/delete")
    public String delete(@PathVariable("applicationId") long applicationId,
            @PathVariable("interviewId") long interviewId,
            @RequestParam(name = "confirmed", defaultValue = "false") boolean confirmed,
            RedirectAttributes redirect) {
        interviews.findById(applicationId, interviewId);
        if (!confirmed) {
            return "redirect:/tracker/" + applicationId + "/interviews/" + interviewId + "/delete";
        }
        interviews.delete(applicationId, interviewId);
        redirect.addFlashAttribute("success", "Interview stage deleted.");
        return "redirect:/tracker/" + applicationId;
    }

    private void prepareForm(long applicationId, Long interviewId, Model model) {
        model.addAttribute("job", applications.findById(applicationId));
        model.addAttribute("editing", interviewId != null);
        model.addAttribute("formAction", "/tracker/" + applicationId + "/interviews"
                + (interviewId == null ? "" : "/" + interviewId + "/edit"));
    }

    @ExceptionHandler({ApplicationNotFoundException.class, InterviewNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Model model) {
        model.addAttribute("errorTitle", "Interview or application not found");
        return "tracker/not-found";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidId() { return "tracker/bad-request"; }
}
