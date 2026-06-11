package com.example.ticketbooking.config;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.TrainRepository;
import com.example.ticketbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Lop khoi tao du lieu ban dau cho co so du lieu (Database Seeder).
 * Tu dong chay khi ung dung khoi hanh nho trien khai giao dien
 * `CommandLineRunner`.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserService userService;
    private final TrainRepository trainRepository;

    /**
     * Ham khoi dung DatabaseSeeder voi cac Dependency Injection can thiet.
     *
     * @param userService     Dich vu quan ly nguoi dung
     * @param trainRepository Kho luu tru thong tin chuyen tau
     */
    @Autowired
    public DatabaseSeeder(UserService userService, TrainRepository trainRepository) {
        this.userService = userService;
        this.trainRepository = trainRepository;
    }

    /**
     * Phuong thuc thuc thi tu dong khi ung dung khoi chay xong.
     * Dung de seed (khoi tao) cac tai khoan mac dinh va chuyen tau mau neu co so du
     * lieu dang rong.
     *
     * @param args Tham so dong lenh truyen vao luc chay ung dung
     * @throws Exception Cac ngoai le xay ra khi ket noi/ghi du lieu vao database
     */
    @Override
    public void run(String... args) throws Exception {
        // Khoi tao cac tai khoan mac dinh neu bang nguoi dung trong
        if (userService.findAllUsers().isEmpty()) {
            User admin = new User("admin", "admin123", "admin@ticketbooking.com", Role.ROLE_ADMIN);
            User seller = new User("seller", "seller123", "seller@ticketbooking.com", Role.ROLE_SELLER);
            User buyer = new User("buyer", "buyer123", "buyer@ticketbooking.com", Role.ROLE_BUYER);

            userService.registerUser(admin);
            userService.registerUser(seller);
            userService.registerUser(buyer);

            System.out.println("Default users seeded:");
            System.out.println(" - admin / admin123 (ROLE_ADMIN)");
            System.out.println(" - seller / seller123 (ROLE_SELLER)");
            System.out.println(" - buyer / buyer123 (ROLE_BUYER)");
        }

        // Khoi tao cac chuyen tau mau neu bang chuyen tau trong
        if (trainRepository.findAll().isEmpty()) {
            // Tim hoac dang ky nguoi ban mac dinh de lien ket voi chuyen tau mau
            User sellerUser;
            try {
                sellerUser = (User) userService.loadUserByUsername("seller");
            } catch (Exception e) {
                User defaultSeller = new User("seller", "seller123", "seller@ticketbooking.com", Role.ROLE_SELLER);
                sellerUser = userService.registerUser(defaultSeller);
            }

            Train train1 = new Train(
                    "SE1",
                    "Hà Nội",
                    "Sài Gòn",
                    LocalDate.now().plusDays(5),
                    LocalTime.of(19, 0),
                    LocalDate.now().plusDays(7),
                    LocalTime.of(4, 30),
                    1200000.0,
                    100,
                    sellerUser);

            Train train2 = new Train(
                    "SE3",
                    "Hà Nội",
                    "Đà Nẵng",
                    LocalDate.now().plusDays(10),
                    LocalTime.of(22, 0),
                    LocalDate.now().plusDays(11),
                    LocalTime.of(14, 0),
                    800000.0,
                    100,
                    sellerUser);

            Train train3 = new Train(
                    "SE5",
                    "Đà Nẵng",
                    "Sài Gòn",
                    LocalDate.now().plusDays(3),
                    LocalTime.of(8, 30),
                    LocalDate.now().plusDays(4),
                    LocalTime.of(0, 30),
                    600000.0,
                    100,
                    sellerUser);

            trainRepository.save(train1);
            trainRepository.save(train2);
            trainRepository.save(train3);

            System.out.println("Default trains seeded successfully.");
        }
    }
}
