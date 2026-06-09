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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xu ly cac tac vu quan ly chuyen tau (Them moi, Chinh sua, Xoa).
 * Cho phep Seller quan ly chuyen tau cua minh va Admin quan tri toan bo chuyen tau tren he thong.
 */
@Controller
public class TrainController {

    private final TrainService trainService;
    private final UserService userService;

    /**
     * Ham khoi dung TrainController.
     *
     * @param trainService Dich vu chuyen tau
     * @param userService Dich vu nguoi dung
     */
    @Autowired
    public TrainController(TrainService trainService, UserService userService) {
        this.trainService = trainService;
        this.userService = userService;
    }

    /**
     * Lay nguoi dung da xac thuc hien tai.
     *
     * @return Doi tuong User neu da dang nhap, nguoc lai tra ve null
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    /**
     * Kiem tra xem nguoi dung hien tai co quyen quan ly chuyen tau nay hay khong.
     * Nguoi co quyen la Admin hoac chinh Seller da tao chuyen tau do.
     *
     * @param train Doi tuong Train can kiem tra
     * @param currentUser Nguoi dung hien tai
     * @return true neu co quyen quan ly, nguoc lai false
     */
    private boolean isAuthorizedToManage(Train train, User currentUser) {
        if (currentUser == null)
            return false;
        if (currentUser.getRole() == Role.ROLE_ADMIN)
            return true;
        return train.getSeller() != null && train.getSeller().getId().equals(currentUser.getId());
    }

    /**
     * Hien thi bieu mau them chuyen tau moi.
     *
     * @param model Doi tuong Model chua thong tin Train moi va co danh dau che do them moi
     * @return Ten template hien thi bieu mau chuyen tau ("train/form")
     */
    @GetMapping("/trains/new")
    public String showCreateForm(Model model) {
        model.addAttribute("train", new Train());
        model.addAttribute("isEdit", false);
        return "train/form";
    }

    /**
     * Xu ly gui bieu mau tao chuyen tau moi.
     *
     * @param train Doi tuong Train chua thong tin gui len tu form
     * @param result Doi tuong chua ket qua kiem tra hop le du lieu
     * @param model Doi tuong Model de truyen loi hoac du lieu sang giao dien
     * @return Duong dan chuyen huong ve Dashboard Seller neu tao thanh cong, nguoc lai hien thi lai form
     */
    @PostMapping("/trains/new")
    public String createTrain(@Valid @ModelAttribute("train") Train train,
            BindingResult result,
            Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null)
            return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "train/form";
        }

        try {
            trainService.saveTrain(train, currentUser);
        } catch (Exception e) {
            model.addAttribute("isEdit", false);
            model.addAttribute("formError", "Failed to create train route: " + e.getMessage());
            return "train/form";
        }

        return "redirect:/dashboard/seller";
    }

    /**
     * Hien thi bieu mau chinh sua chuyen tau hien tai.
     * Kiem tra phan quyen de dam bao chi nguoi so huu chuyen tau hoac Admin moi duoc sua.
     *
     * @param id ID cua chuyen tau can sua
     * @param model Doi tuong Model
     * @return Ten template hien thi bieu mau ("train/form")
     */
    @GetMapping("/trains/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null)
            return "redirect:/login";

        Train train = trainService.getTrainById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train route not found with ID: " + id));

        if (!isAuthorizedToManage(train, currentUser)) {
            return "redirect:/dashboard?error=unauthorized";
        }

        model.addAttribute("train", train);
        model.addAttribute("isEdit", true);
        return "train/form";
    }

    /**
     * Xu ly gui bieu mau cap nhat chuyen tau.
     *
     * @param id ID cua chuyen tau
     * @param train Doi tuong Train chua thong tin cap nhat
     * @param result Doi tuong chua ket qua xac thuc hop le du lieu
     * @param model Doi tuong Model
     * @return Duong dan chuyen huong ve Dashboard Seller neu thanh cong, nguoc lai hien thi lai form
     */
    @PostMapping("/trains/edit/{id}")
    public String updateTrain(@PathVariable("id") Long id,
            @Valid @ModelAttribute("train") Train train,
            BindingResult result,
            Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null)
            return "redirect:/login";

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

    /**
     * Xu ly xoa chuyen tau.
     * Chi cho phep nguoi co tham quyen xoa, va chi khi chuyen tau chua co ve nao duoc dat.
     *
     * @param id ID cua chuyen tau can xoa
     * @param redirectAttributes Luu thong bao thanh cong hoac loi de hien thi o trang tiep theo sau khi redirect
     * @return Chuyen huong ve trang Dashboard Admin hoac Dashboard Seller tuy thuoc vai tro
     */
    @PostMapping("/trains/delete/{id}")
    public String deleteTrain(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null)
            return "redirect:/login";

        try {
            Train train = trainService.getTrainById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Train route not found with ID: " + id));

            if (!isAuthorizedToManage(train, currentUser)) {
                return "redirect:/dashboard?error=unauthorized";
            }

            trainService.deleteTrain(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xoa chuyen tau thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xoa chuyen tau that bai: " + e.getMessage());
        }

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return "redirect:/dashboard/admin";
        }
        return "redirect:/dashboard/seller";
    }
}
