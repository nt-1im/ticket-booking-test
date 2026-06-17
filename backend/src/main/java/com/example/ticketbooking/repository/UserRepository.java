package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Giao dien UserRepository cung cap cac phuong thuc truy xuat du lieu tu bang nguoi dung (User).
 * Ke thua JpaRepository de thua huong cac thao tac CRUD co ban.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Tim kiem nguoi dung dua tren ten dang nhap.
     *
     * @param username Ten dang nhap cua nguoi dung can tim
     * @return Mot Optional chua thong tin User neu tim thay, nguoc lai tra ve Optional rong
     */
    Optional<User> findByUsername(String username);

    /**
     * Kiem tra su ton tai cua nguoi dung dua tren ten dang nhap.
     *
     * @param username Ten dang nhap can kiem tra
     * @return true neu ten dang nhap da duoc su dung, nguoc lai tra ve false
     */
    boolean existsByUsername(String username);

    /**
     * Kiem tra su ton tai cua nguoi dung dua tren dia chi email.
     *
     * @param email Dia chi email can kiem tra
     * @return true neu email da duoc dang ky trong he thong, nguoc lai tra ve false
     */
    boolean existsByEmail(String email);
}
