package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.TrainRepository;
import com.example.ticketbooking.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Dich vu xu ly cac nghiep vu lien quan den Ve tau (Ticket).
 * Bao gom cac chuc nang cot loi nhu dat ve (co khoa bi quan tranh tranh chap ghe), huy ve va khoi phuc ghe trong.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TrainRepository trainRepository;

    /**
     * Ham khoi dung TicketService.
     *
     * @param ticketRepository Repository quan ly ve tau
     * @param trainRepository Repository quan ly chuyen tau
     */
    @Autowired
    public TicketService(TicketRepository ticketRepository, TrainRepository trainRepository) {
        this.ticketRepository = ticketRepository;
        this.trainRepository = trainRepository;
    }

    /**
     * Lay danh sach toan bo ve trong he thong.
     *
     * @return Danh sach tat ca cac ve
     */
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Lay danh sach ve da dat cua mot nguoi mua (Buyer) cu the, sap xep theo ngay dat moi nhat.
     *
     * @param buyerId ID cua khach mua ve
     * @return Danh sach ve cua khach hang do
     */
    public List<Ticket> getTicketsByBuyer(Long buyerId) {
        return ticketRepository.findByBuyerIdOrderByBookingDateDesc(buyerId);
    }

    /**
     * Lay danh sach ve da dat tu cac chuyen tau cua mot nguoi ban (Seller) cu the.
     *
     * @param sellerId ID cua nguoi ban
     * @return Danh sach ve tuong ung voi cac chuyen tau cua nguoi ban do
     */
    public List<Ticket> getTicketsBySeller(Long sellerId) {
        return ticketRepository.findByTrainSellerIdOrderByBookingDateDesc(sellerId);
    }

    /**
     * Tim thong tin ve theo ID.
     *
     * @param id ID cua ve can tim
     * @return Mot Optional chua ve tau neu tim thay
     */
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    /**
     * Dat ve tau moi cho hanh khach.
     * Ap dung khoa bi quan tren thuc the Train de ngan chan dat trung ghe khi nhieu luong thuc hien dong thoi.
     * Thuat toan tu dong tim va phan bo khoi ghe lien tuc trong dau tien trong toa tau cho khach.
     *
     * @param trainId ID chuyen tau can dat ve
     * @param buyer Tai khoan thuc hien dat ve
     * @param quantity So luong ve (ghe) muon dat
     * @param passengerName Ho ten hanh khach di tau
     * @param passengerIdCard So CMND/CCCD hanh khach
     * @return Doi tuong Ticket sau khi luu thanh cong vao co so du lieu
     * @throws IllegalArgumentException Nem ra khi tham so dau vao khong hop le, khong du ghe trong, hoac khong tim thay khoi ghe trong lien ke phu hop.
     */
    @Transactional
    public Ticket bookTicket(Long trainId, User buyer, int quantity, String passengerName, String passengerIdCard) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (passengerName == null || passengerName.isBlank()) {
            throw new IllegalArgumentException("Passenger name is required");
        }
        if (passengerIdCard == null || passengerIdCard.isBlank()) {
            throw new IllegalArgumentException("Passenger ID Card / Passport is required");
        }

        // Tim kiem va ap dung khoa ghi bi quan (Pessimistic Write Lock) tren chuyen tau
        Train train = trainRepository.findByIdForUpdate(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found with ID: " + trainId));

        if (train.getAvailableSeats() < quantity) {
            throw new IllegalArgumentException("Not enough seats available! Only " + train.getAvailableSeats() + " left.");
        }

        // Lay danh sach ve dang hoat dong (chua huy) de tinh toan vi tri ghe da bi chiem
        List<Ticket> activeTickets = ticketRepository.findByTrainId(trainId).stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .toList();

        boolean[] occupied = new boolean[train.getTotalSeats() + 1];
        for (Ticket t : activeTickets) {
            int ticketStartIdx = (t.getCarriageNumber() - 1) * 10 + t.getSeatNumber();
            for (int i = 0; i < t.getQuantity(); i++) {
                int idx = ticketStartIdx + i;
                if (idx < occupied.length) {
                    occupied[idx] = true;
                }
            }
        }

        // Thuat toan: Tim khoi ghe trong lien ke co kich thuoc tuong duong voi so luong ve dat
        int startSeatIndex = -1;
        for (int i = 1; i <= train.getTotalSeats() - quantity + 1; i++) {
            boolean blockFree = true;
            for (int j = 0; j < quantity; j++) {
                if (occupied[i + j]) {
                    blockFree = false;
                    break;
                }
            }
            if (blockFree) {
                startSeatIndex = i;
                break;
            }
        }

        if (startSeatIndex == -1) {
            throw new IllegalArgumentException("No contiguous block of " + quantity + " seats is available. Try booking fewer tickets at a time.");
        }

        // Tinh toan so toa va so ghe dua tren vi tri bat dau (moi toa chua toi da 10 ghe)
        int carriageNumber = (startSeatIndex - 1) / 10 + 1;
        int seatNumber = (startSeatIndex - 1) % 10 + 1;

        // Tru bot so ghe trong cua chuyen tau
        train.setAvailableSeats(train.getAvailableSeats() - quantity);
        trainRepository.save(train);

        // Tao doi tuong ve va luu vao co so du lieu
        double totalPrice = train.getPrice() * quantity;
        Ticket ticket = new Ticket(train, buyer, passengerName, passengerIdCard, carriageNumber, seatNumber, quantity, totalPrice, LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    /**
     * Huy ve tau da dat.
     * Khoi phuc so ghe trong cho chuyen tau tuong ung. Chi cho phep Admin hoac chinh khach hang mua ve do thuc hien huy.
     *
     * @param ticketId ID cua ve can huy
     * @param currentUser Nguoi dung hien tai dang thuc hien thao tac huy ve
     * @throws SecurityException Nem ra khi nguoi dung hien tai khong co quyen huy ve nay
     * @throws IllegalArgumentException Nem ra khi ve khong ton tai hoac da duoc huy truoc do
     */
    @Transactional
    public void cancelTicket(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        // Kiem tra phan quyen: Chi ADMIN hoac chinh khach mua ve (BUYER so huu ve) moi co quyen huy
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");
        boolean isBuyer = ticket.getBuyer().getId().equals(currentUser.getId());

        if (!isAdmin && !isBuyer) {
            throw new SecurityException("You are not authorized to cancel this ticket!");
        }

        if (ticket.getStatus() == Ticket.Status.CANCELLED) {
            throw new IllegalArgumentException("Ticket is already cancelled!");
        }

        // Tai thong tin chuyen tau bang khoa bi quan de khoi phuc so luong ghe trong mot cach an toan
        Train train = trainRepository.findByIdForUpdate(ticket.getTrain().getId())
                .orElseThrow(() -> new IllegalArgumentException("Train not found with ID: " + ticket.getTrain().getId()));

        // Tra lai so luong ghe trong
        train.setAvailableSeats(train.getAvailableSeats() + ticket.getQuantity());
        trainRepository.save(train);

        // Cap nhat trang thai ve thanh CANCELLED
        ticket.setStatus(Ticket.Status.CANCELLED);
        ticketRepository.save(ticket);
    }

    /**
     * Lay danh sach cac ghe da duoc dat (chi so tuyet doi tu 1 den totalSeats) tren chuyen tau.
     */
    public List<Integer> getOccupiedSeats(Long trainId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found with ID: " + trainId));
        List<Ticket> activeTickets = ticketRepository.findByTrainId(trainId).stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .toList();

        List<Integer> occupied = new java.util.ArrayList<>();
        for (Ticket t : activeTickets) {
            int ticketStartIdx = (t.getCarriageNumber() - 1) * 10 + t.getSeatNumber();
            for (int i = 0; i < t.getQuantity(); i++) {
                int idx = ticketStartIdx + i;
                if (idx <= train.getTotalSeats()) {
                    occupied.add(idx);
                }
            }
        }
        return occupied;
    }

    /**
     * Dat cac ghe duoc chon tren tau cho khach hang.
     */
    @Transactional
    public List<Ticket> bookTicketWithSeats(Long trainId, User buyer, List<Integer> selectedSeats, String passengerName, String passengerIdCard) {
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected");
        }
        if (passengerName == null || passengerName.isBlank()) {
            throw new IllegalArgumentException("Passenger name is required");
        }
        if (passengerIdCard == null || passengerIdCard.isBlank()) {
            throw new IllegalArgumentException("Passenger ID Card / Passport is required");
        }

        // Tim kiem va ap dung khoa ghi bi quan
        Train train = trainRepository.findByIdForUpdate(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found with ID: " + trainId));

        int quantity = selectedSeats.size();
        if (train.getAvailableSeats() < quantity) {
            throw new IllegalArgumentException("Not enough seats available! Only " + train.getAvailableSeats() + " left.");
        }

        // Kiem tra tung ghe xem co bi dat trung hoac vuot qua so ghe thiet ke khong
        List<Integer> occupied = getOccupiedSeats(trainId);
        for (Integer seatIdx : selectedSeats) {
            if (seatIdx < 1 || seatIdx > train.getTotalSeats()) {
                throw new IllegalArgumentException("Invalid seat selection: " + seatIdx);
            }
            if (occupied.contains(seatIdx)) {
                throw new IllegalArgumentException("Seat " + seatIdx + " is already booked.");
            }
        }

        // Tao rieng biet tung ve co quantity = 1 cho tung cho ngoi duoc dat
        List<Ticket> bookedTickets = new java.util.ArrayList<>();
        double pricePerSeat = train.getPrice();
        for (Integer seatIdx : selectedSeats) {
            int carriageNumber = (seatIdx - 1) / 10 + 1;
            int seatNumber = (seatIdx - 1) % 10 + 1;

            Ticket ticket = new Ticket(
                train,
                buyer,
                passengerName,
                passengerIdCard,
                carriageNumber,
                seatNumber,
                1,
                pricePerSeat,
                LocalDateTime.now()
            );
            bookedTickets.add(ticketRepository.save(ticket));
        }

        // Giam bot so ghe trong tren chuyen tau
        train.setAvailableSeats(train.getAvailableSeats() - quantity);
        trainRepository.save(train);

        return bookedTickets;
    }
}

