package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository de thao tac voi co so du lieu cho thuc the Route.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    
    /**
     * Tim cac tuyen duong co dinh thuoc ve mot nguoi ban (Seller) cu the.
     *
     * @param sellerId ID cua nguoi ban
     * @return Danh sach cac tuyen duong
     */
    List<Route> findBySellerId(Long sellerId);
}
