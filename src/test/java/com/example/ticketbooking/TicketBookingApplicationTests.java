package com.example.ticketbooking;

import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.TicketRepository;
import com.example.ticketbooking.repository.TrainRepository;
import com.example.ticketbooking.repository.UserRepository;
import com.example.ticketbooking.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TicketBookingApplicationTests {

	@Autowired
	private TicketService ticketService;

	@Autowired
	private TrainRepository trainRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testConcurrentBooking() throws InterruptedException {
		// 1. Create a test seller and buyer
		User seller = userRepository.findByUsername("seller").orElseGet(() -> {
			User u = new User("seller", "seller123", "seller@test.com", Role.ROLE_SELLER);
			return userRepository.save(u);
		});
		User buyer = userRepository.findByUsername("buyer").orElseGet(() -> {
			User u = new User("buyer", "buyer123", "buyer@test.com", Role.ROLE_BUYER);
			return userRepository.save(u);
		});

		// 2. Create a test train with exactly 5 seats
		Train train = new Train(
				"CONC_TEST",
				"Hanoi",
				"Saigon",
				LocalDate.now().plusDays(10),
				LocalTime.of(12, 0),
				500000.0,
				5,
				seller);
		train = trainRepository.save(train);
		Long trainId = train.getId();

		// 3. Spawn 10 concurrent booking requests, each trying to book 1 seat
		int threadCount = 10;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(1);
		CountDownLatch finishedLatch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		for (int i = 0; i < threadCount; i++) {
			final int index = i;
			executor.submit(() -> {
				try {
					latch.await(); // Wait for signal to start all at once
					ticketService.bookTicket(trainId, buyer, 1, "Passenger " + index, "ID_" + index);
					successCount.incrementAndGet();
				} catch (Exception e) {
					failureCount.incrementAndGet();
				} finally {
					finishedLatch.countDown();
				}
			});
		}

		// Release all threads at once
		latch.countDown();
		finishedLatch.await();
		executor.shutdown();

		// 4. Verify results
		// Since there are only 5 seats, exactly 5 bookings should succeed and 5 should
		// fail
		assertEquals(5, successCount.get(), "Exactly 5 bookings should succeed");
		assertEquals(5, failureCount.get(), "Exactly 5 bookings should fail due to lack of seats");

		// Fetch the updated train from DB
		Train updatedTrain = trainRepository.findById(trainId).orElseThrow();
		assertEquals(0, updatedTrain.getAvailableSeats(), "Available seats should be 0");

		// Check the tickets created in the DB
		List<Ticket> tickets = ticketRepository.findByTrainId(trainId);
		assertEquals(5, tickets.size(), "There should be exactly 5 ticket records in DB");

		// Check that seat numbers are unique and between 1 and 5
		List<Integer> seatNumbers = new ArrayList<>();
		for (Ticket t : tickets) {
			assertEquals(1, t.getCarriageNumber(), "All tickets should be in Carriage 1");
			assertTrue(t.getSeatNumber() >= 1 && t.getSeatNumber() <= 5, "Seat number should be between 1 and 5");
			seatNumbers.add(t.getSeatNumber());
		}
		// Check uniqueness by comparing size with set size
		assertEquals(5, (int) seatNumbers.stream().distinct().count(), "All assigned seat numbers must be unique");
	}
}
