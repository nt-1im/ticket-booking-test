package com.example.ticketbooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Thuc the Train dai dien cho bang chuyen tau "trains" trong co so du lieu.
 * Chua thong tin ve so hieu tau, ga di, ga den, lich khoi hanh, so ghe va nguoi ban (Seller) so huu chuyen tau.
 */
@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Train number is required")
    @Column(name = "train_number", nullable = false)
    private String trainNumber;

    @NotBlank(message = "Departure station cannot be blank")
    @Column(name = "departure_station", nullable = false)
    private String departureStation;

    @NotBlank(message = "Arrival station cannot be blank")
    @Column(name = "arrival_station", nullable = false)
    private String arrivalStation;

    @NotNull(message = "Departure date is required")
    @FutureOrPresent(message = "Departure date must be in the present or future")
    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @NotNull(message = "Departure time is required")
    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Min(value = 0, message = "Price cannot be negative")
    @Column(nullable = false)
    private double price;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /**
     * Ham khoi dung mac dinh khong tham so (yeu cau boi JPA).
     */
    public Train() {}

    /**
     * Ham khoi dung day du tham so de tao mot chuyen tau moi.
     *
     * @param trainNumber So hieu chuyen tau (vi du: SE1)
     * @param departureStation Ga xuat phat
     * @param arrivalStation Ga den
     * @param departureDate Ngay di
     * @param departureTime Gio di
     * @param price Gia ve cho mot ghe
     * @param totalSeats Tong so luong ghe tren tau
     * @param seller Tai khoan nguoi ban (Seller) dang chuyen tau nay
     */
    public Train(String trainNumber, String departureStation, String arrivalStation, LocalDate departureDate, LocalTime departureTime, double price, int totalSeats, User seller) {
        this.trainNumber = trainNumber;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.seller = seller;
    }

    /**
     * Lay ID duy nhat cua chuyen tau.
     *
     * @return ID chuyen tau
     */
    public Long getId() {
        return id;
    }

    /**
     * Thiet lap ID duy nhat cho chuyen tau.
     *
     * @param id ID moi
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lay so hieu tau.
     *
     * @return So hieu tau (String)
     */
    public String getTrainNumber() {
        return trainNumber;
    }

    /**
     * Thiet lap so hieu tau moi.
     *
     * @param trainNumber So hieu tau moi
     */
    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    /**
     * Lay ten ga xuat phat.
     *
     * @return Ten ga di
     */
    public String getDepartureStation() {
        return departureStation;
    }

    /**
     * Thiet lap ga xuat phat.
     *
     * @param departureStation Ten ga di moi
     */
    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    /**
     * Lay ten ga den.
     *
     * @return Ten ga den
     */
    public String getArrivalStation() {
        return arrivalStation;
    }

    /**
     * Thiet lap ga den.
     *
     * @param arrivalStation Ten ga den moi
     */
    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    /**
     * Lay ngay khoi hanh.
     *
     * @return Ngay khoi hanh (LocalDate)
     */
    public LocalDate getDepartureDate() {
        return departureDate;
    }

    /**
     * Thiet lap ngay khoi hanh moi.
     *
     * @param departureDate Ngay khoi hanh moi
     */
    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    /**
     * Lay gio khoi hanh.
     *
     * @return Gio khoi hanh (LocalTime)
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Thiet lap gio khoi hanh moi.
     *
     * @param departureTime Gio khoi hanh moi
     */
    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * Lay gia ve cua mot ghe.
     *
     * @return Don gia ve (double)
     */
    public double getPrice() {
        return price;
    }

    /**
     * Thiet lap gia ve.
     *
     * @param price Gia ve moi
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Lay tong so ghe thiet ke tren tau.
     *
     * @return Tong so ghe
     */
    public int getTotalSeats() {
        return totalSeats;
    }

    /**
     * Thiet lap tong so ghe tren tau.
     *
     * @param totalSeats Tong so ghe moi
     */
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    /**
     * Lay so ghe con trong hien tai cua chuyen tau.
     *
     * @return So ghe trong
     */
    public int getAvailableSeats() {
        return availableSeats;
    }

    /**
     * Thiet lap so ghe con trong.
     *
     * @param availableSeats So ghe trong moi
     */
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    /**
     * Lay thong tin nguoi ban (Seller) so huu chuyen tau nay.
     *
     * @return Doi tuong User dai dien cho nguoi ban
     */
    public User getSeller() {
        return seller;
    }

    /**
     * Thiet lap nguoi ban so huu chuyen tau nay.
     *
     * @param seller Doi tuong User (Seller) moi
     */
    public void setSeller(User seller) {
        this.seller = seller;
    }
}
