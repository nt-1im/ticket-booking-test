package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.service.TrainService;
import com.example.ticketbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TrainController {

    private final TrainService trainService;
    private final UserService userService;

    @Autowired
    public TrainController(TrainService trainService, UserService userService) {
        this.trainService = trainService;
        this.userService = userService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    private boolean isAuthorizedToManage(Train train, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ROLE_ADMIN) return true;
        return train.getSeller() != null && train.getSeller().getId().equals(currentUser.getId());
    }

    @GetMapping("/trains/new")
    public String showCreateForm(Model model) {
        model.addAttribute("train", new Train());
        return "train/form";
    }

    @PostMapping("/trains/new")
    public String createTrain(@Valid @ModelAttribute("train") Train train,
                              BindingResult result,
                              Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        if (result.hasErrors()) {
            return "train/form";
        }

        try {
            trainService.saveTrain(train, currentUser);
        } catch (Exception e) {
            model.addAttribute("formError", "Failed to create train route: " + e.getMessage());
            return "train/form";
        }

        return "redirect:/dashboard/seller";
    }

    @GetMapping("/trains/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        Train train = trainService.getTrainById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train route not found with ID: " + id));

        if (!isAuthorizedToManage(train, currentUser)) {
            return "redirect:/dashboard?error=unauthorized";
        }

        model.addAttribute("train", train);
        model.addAttribute("isEdit", true);
        return "train/form";
    }

    @PostMapping("/trains/edit/{id}")
    public String updateTrain(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("train") Train train,
                              BindingResult result,
                              Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        Train existingTrain = trainService.getTrainById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train route not found with ID: " + id));

        if (!isAuthorizedToManage(existingTrain, currentUser)) {
            return "redirect:/dashboard?error=unauthorized";
        }

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "train/form";
        }

        try {
            trainService.updateTrain(id, train);
        } catch (Exception e) {
            model.addAttribute("isEdit", true);
            model.addAttribute("formError", "Failed to update train: " + e.getMessage());
            return "train/form";
        }

        return "redirect:/dashboard/seller";
    }

    @PostMapping("/trains/delete/{id}")
    public String deleteTrain(@PathVariable("id") Long id) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        Train train = trainService.getTrainById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train route not found with ID: " + id));

        if (!isAuthorizedToManage(train, currentUser)) {
            return "redirect:/dashboard?error=unauthorized";
        }

        trainService.deleteTrain(id);
        return "redirect:/dashboard/seller";
    }
}
