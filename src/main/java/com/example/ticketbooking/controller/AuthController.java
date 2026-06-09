package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.User;
import com.example.ticketbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller xu ly cac yeu cau lien quan den xac thuc va phan quyen nhu dang nhap va dang ky tai khoan.
 */
@Controller
public class AuthController {

    private final UserService userService;

    /**
     * Ham khoi dung AuthController.
     *
     * @param userService Dich vu quan ly nguoi dung
     */
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Hien thi trang dang nhap.
     *
     * @return Ten template hien thi trang dang nhap ("login")
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Hien thi bieu mau dang ky tai khoan moi.
     *
     * @param model Doi tuong chua thuc the User rong de lien ket du lieu voi form dang ky
     * @return Ten template hien thi trang dang ky ("register")
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Xu ly gui bieu mau dang ky nguoi dung moi.
     * Tien hanh xac thuc dinh dang du lieu dau vao va kiem tra su trung lap ten dang nhap/email.
     *
     * @param user Doi tuong User chua thong tin tu bieu mau dang ky
     * @param result Doi tuong BindingResult chua ket qua kiem tra hop le du lieu (validation)
     * @param model Doi tuong chua thong bao loi dang ky neu xay ra ngoai le
     * @return Duong dan chuyen huong den trang dang nhap neu dang ky thanh cong, nguoc lai tra ve trang dang ky cung thong bao loi
     */
    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") User user,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken");
            return "register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already taken");
            return "register";
        }

        try {
            userService.registerUser(user);
        } catch (Exception e) {
            model.addAttribute("registrationError", "Failed to register user: " + e.getMessage());
            return "register";
        }

        return "redirect:/login?registered=true";
    }
}
