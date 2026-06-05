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

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserService userService;
    private final TrainRepository trainRepository;

    @Autowired
    public DatabaseSeeder(UserService userService, TrainRepository trainRepository) {
        this.userService = userService;
        this.trainRepository = trainRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed users if empty
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

            // Seed trains if empty
            if (trainRepository.findAll().isEmpty()) {
                // Fetch the seller user from DB to ensure JPA session is correct
                User sellerUser = (User) userService.loadUserByUsername("seller");

                Train train1 = new Train(
                        "SE1",
                        "Hà Nội",
                        "Sài Gòn",
                        LocalDate.now().plusDays(5),
                        LocalTime.of(19, 0),
                        1200000.0,
                        100,
                        sellerUser
                );

                Train train2 = new Train(
                        "SE3",
                        "Hà Nội",
                        "Đà Nẵng",
                        LocalDate.now().plusDays(10),
                        LocalTime.of(22, 0),
                        800000.0,
                        100,
                        sellerUser
                );

                Train train3 = new Train(
                        "SE5",
                        "Đà Nẵng",
                        "Sài Gòn",
                        LocalDate.now().plusDays(3),
                        LocalTime.of(8, 30),
                        600000.0,
                        100,
                        sellerUser
                );

                trainRepository.save(train1);
                trainRepository.save(train2);
                trainRepository.save(train3);

                System.out.println("Default trains seeded successfully.");
            }
        }
    }
}

