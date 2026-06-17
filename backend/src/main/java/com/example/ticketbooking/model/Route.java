package com.example.ticketbooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Thuc the Route dai dien cho tuyen duong co dinh do nguoi ban (Seller) tao ra.
 * Tuyen duong bao gom ga di va ga den.
 */
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Departure station cannot be blank")
    @Column(name = "departure_station", nullable = false)
    private String departureStation;

    @NotBlank(message = "Arrival station cannot be blank")
    @Column(name = "arrival_station", nullable = false)
    private String arrivalStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    public Route() {}

    public Route(String departureStation, String arrivalStation, User seller) {
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.seller = seller;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }
}
