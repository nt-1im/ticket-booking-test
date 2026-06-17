package com.example.ticketbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lop khoi chay ung dung chinh cho he thong Dat Ve Tau (Ticket Booking).
 * Su dung cau hinh tu dong cua Spring Boot thong qua `@SpringBootApplication`.
 */
@SpringBootApplication
public class TicketBookingApplication {

	/**
	 * Phuong thuc khoi chay chinh (main entry point) cua ung dung Spring Boot.
	 *
	 * @param args Tham so dong lenh truyen vao khi khoi dong ung dung
	 */
	public static void main(String[] args) {
		SpringApplication.run(TicketBookingApplication.class, args);
	}

}
