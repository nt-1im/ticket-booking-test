package com.example.ticketbooking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Thuc the Ticket dai dien cho bang ve tau "tickets" trong co so du lieu.
 * Ghi lai cac chi tiet dat ve bao gom chuyen tau, khach hang (Buyer), thong tin hanh khach, so toa, so ghe va trang thai ve.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    /**
     * Enum Status dinh nghia trang thai cua ve tau.
     */
    public enum Status {
        /** Ve da dat va thanh toan thanh cong. */
        BOOKED,
        /** Ve da bi huy. */
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "passenger_id_card", nullable = false)
    private String passengerIdCard;

    @Column(name = "carriage_number", nullable = false)
    private int carriageNumber;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.BOOKED;

    /**
     * Ham khoi dung mac dinh khong tham so (yeu cau boi JPA).
     */
    public Ticket() {
    }

    /**
     * Ham khoi dung day du tham so de tao mot ve tau moi.
     *
     * @param train Chuyen tau lien ket voi ve
     * @param buyer Tai khoan nguoi mua ve
     * @param passengerName Ho ten hanh khach di tau
     * @param passengerIdCard So CMND/CCCD cua hanh khach
     * @param carriageNumber So hieu toa tau
     * @param seatNumber So hieu ghe ngoi
     * @param quantity So luong ve (thuong la 1)
     * @param totalPrice Tong tien thanh toan cho ve
     * @param bookingDate Thoi gian dat ve
     */
    public Ticket(Train train, User buyer, String passengerName, String passengerIdCard, int carriageNumber,
            int seatNumber, int quantity, double totalPrice, LocalDateTime bookingDate) {
        this.train = train;
        this.buyer = buyer;
        this.passengerName = passengerName;
        this.passengerIdCard = passengerIdCard;
        this.carriageNumber = carriageNumber;
        this.seatNumber = seatNumber;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.bookingDate = bookingDate;
        this.status = Status.BOOKED;
    }

    /**
     * Lay ID duy nhat cua ve.
     *
     * @return ID ve
     */
    public Long getId() {
        return id;
    }

    /**
     * Thiet lap ID duy nhat cua ve.
     *
     * @param id ID moi
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lay thong tin chuyen tau lien ket voi ve nay.
     *
     * @return Doi tuong Train
     */
    public Train getTrain() {
        return train;
    }

    /**
     * Gan chuyen tau cho ve.
     *
     * @param train Chuyen tau moi
     */
    public void setTrain(Train train) {
        this.train = train;
    }

    /**
     * Lay thong tin tai khoan nguoi mua ve.
     *
     * @return Doi tuong User (Buyer)
     */
    public User getBuyer() {
        return buyer;
    }

    /**
     * Thiet lap nguoi mua ve.
     *
     * @param buyer Doi tuong User nguoi mua
     */
    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    /**
     * Lay ho ten hanh khach di tau.
     *
     * @return Ten hanh khach
     */
    public String getPassengerName() {
        return passengerName;
    }

    /**
     * Thiet lap ho ten hanh khach.
     *
     * @param passengerName Ten hanh khach moi
     */
    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    /**
     * Lay so giay to tuy than (CMND/CCCD) cua hanh khach.
     *
     * @return So CMND/CCCD
     */
    public String getPassengerIdCard() {
        return passengerIdCard;
    }

    /**
     * Thiet lap so giay to tuy than cua hanh khach.
     *
     * @param passengerIdCard So CMND/CCCD moi
     */
    public void setPassengerIdCard(String passengerIdCard) {
        this.passengerIdCard = passengerIdCard;
    }

    /**
     * Lay so toa tau.
     *
     * @return So toa tau
     */
    public int getCarriageNumber() {
        return carriageNumber;
    }

    /**
     * Thiet lap so toa tau cho ve.
     *
     * @param carriageNumber So toa moi
     */
    public void setCarriageNumber(int carriageNumber) {
        this.carriageNumber = carriageNumber;
    }

    /**
     * Lay so thu tu ghe tren toa.
     *
     * @return So ghe
     */
    public int getSeatNumber() {
        return seatNumber;
    }

    /**
     * Thiet lap so thu tu ghe.
     *
     * @param seatNumber So ghe moi
     */
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    /**
     * Lay so luong ve dat (thuong la 1).
     *
     * @return So luong ve
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Thiet lap so luong ve dat.
     *
     * @param quantity So luong moi
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Lay tong gia tien cua ve nay.
     *
     * @return Tong tien (double)
     */
    public double getTotalPrice() {
        return totalPrice;
    }

    /**
     * Thiet lap tong tien cho ve.
     *
     * @param totalPrice Tong tien moi
     */
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Lay ngay gio thuc hien dat ve.
     *
     * @return Thoi diem dat ve (LocalDateTime)
     */
    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    /**
     * Thiet lap ngay gio dat ve.
     *
     * @param bookingDate Thoi diem moi
     */
    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    /**
     * Lay trang thai hien tai cua ve (BOOKED hoac CANCELLED).
     *
     * @return Trang thai ve (Status)
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Thiet lap trang thai ve moi.
     *
     * @param status Trang thai moi
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
