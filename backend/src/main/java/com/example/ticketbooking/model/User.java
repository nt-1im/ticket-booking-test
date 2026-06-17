package com.example.ticketbooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Thuc the User dai dien cho bang nguoi dung "users" trong co so du lieu.
 * Lop nay trien khai giao dien UserDetails cua Spring Security de phuc vu xac thuc va phan quyen.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Ham khoi dung mac dinh khong tham so (yeu cau boi JPA).
     */
    public User() {}

    /**
     * Ham khoi dung co tham so day du thong tin co ban cho tai khoan moi.
     *
     * @param username Ten dang nhap
     * @param password Mat khau da duoc ma hoa hoac chua ma hoa
     * @param email Dia chi email
     * @param role Vai tro cua nguoi dung
     */
    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.enabled = true;
    }

    /**
     * Lay danh sach cac quyen han (Role) cua nguoi dung duoc anh xa sang dinh dang cua Spring Security.
     *
     * @return Danh sach chua quyen han duy nhat cua nguoi dung duoi dang GrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Lay mat khau da ma hoa cua nguoi dung.
     *
     * @return Chuoi mat khau bam
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Lay ten dang nhap cua nguoi dung.
     *
     * @return Ten dang nhap cua tai khoan
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Kiem tra xem tai khoan co bi het han hay khong.
     *
     * @return Luon tra ve true (mac dinh khong het han tai khoan)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Kiem tra xem tai khoan co bi khoa hay khong.
     *
     * @return Luon tra ve true (mac dinh khong khoa tai khoan)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Kiem tra xem thong tin xac thuc (mat khau) co bi het han hay khong.
     *
     * @return Luon tra ve true (mac dinh thong tin xac thuc khong het han)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Kiem tra xem tai khoan nguoi dung co dang hoat dong hay da bi vo hieu hoa.
     *
     * @return true neu tai khoan dang hoat dong, nguoc lai tra ve false
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Lay ID duy nhat cua nguoi dung.
     *
     * @return ID nguoi dung
     */
    public Long getId() {
        return id;
    }

    /**
     * Thiet lap ID duy nhat cua nguoi dung.
     *
     * @param id ID nguoi dung moi
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Thiet lap ten dang nhap moi cho nguoi dung.
     *
     * @param username Ten dang nhap moi
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Thiet lap mat khau moi cho nguoi dung.
     *
     * @param password Mat khau moi (thuong da duoc ma hoa)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Lay dia chi email cua nguoi dung.
     *
     * @return Email cua nguoi dung
     */
    public String getEmail() {
        return email;
    }

    /**
     * Thiet lap dia chi email moi cho nguoi dung.
     *
     * @param email Email moi
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Lay vai tro (Role) hien tai cua nguoi dung.
     *
     * @return Doi tuong enum Role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Thiet lap vai tro (Role) moi cho nguoi dung.
     *
     * @param role Vai tro moi can gan
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Thiet lap trang thai hoat dong (kich hoat hoac vo hieu hoa) cua tai khoan.
     *
     * @param enabled Trang thai hoat dong moi
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
