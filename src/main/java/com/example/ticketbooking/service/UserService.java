package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Dich vu xu ly cac nghiep vu lien quan den nguoi dung (User).
 * Trien khai giao dien UserDetailsService de Spring Security goi khi xac thuc dang nhap.
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Ham khoi dung UserService.
     *
     * @param userRepository Repository truy xuat bang nguoi dung
     * @param passwordEncoder Doi tuong ma hoa mat khau
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Tai thong tin nguoi dung tu co so du lieu dua vao ten dang nhap.
     * Phuong thuc bat buoc cua giao dien UserDetailsService.
     *
     * @param username Ten dang nhap nguoi dung nhap vao
     * @return Doi tuong UserDetails chua thong tin va quyen han nguoi dung
     * @throws UsernameNotFoundException Nem ra khi khong tim thay ten dang nhap trong he thong
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Dang ky tai khoan nguoi dung moi.
     * Kiem tra tinh hop le cua ten dang nhap va email, dong thoi ma hoa mat khau truoc khi luu.
     *
     * @param user Thuc the User chua thong tin dang ky
     * @return Thuc the User sau khi da luu thanh cong vao database
     * @throws IllegalArgumentException Nem ra khi ten dang nhap hoac email da duoc su dung
     */
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Kiem tra su ton tai cua ten dang nhap trong he thong.
     *
     * @param username Ten dang nhap can kiem tra
     * @return true neu da ton tai, nguoc lai false
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Kiem tra su ton tai cua email trong he thong.
     *
     * @param email Dia chi email can kiem tra
     * @return true neu da ton tai, nguoc lai false
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Tim kiem va tra ve toan bo danh sach nguoi dung trong he thong.
     *
     * @return Danh sach chua tat ca User
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Tim kiem nguoi dung dua tren ID duy nhat.
     *
     * @param id ID cua nguoi dung can tim
     * @return Mot Optional chua User neu tim thay
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Thay doi trang thai hoat dong (Kich hoat/Vo hieu hoa) cua tai khoan nguoi dung.
     *
     * @param userId ID cua nguoi dung can chuyen trang thai
     */
    @Transactional
    public void toggleUserStatus(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
        });
    }

    /**
     * Cap nhat vai tro (Role) moi cho nguoi dung.
     *
     * @param userId ID cua nguoi dung can cap nhat
     * @param role Vai tro moi can gan (ADMIN, SELLER, BUYER)
     */
    @Transactional
    public void updateUserRole(Long userId, Role role) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }
}
