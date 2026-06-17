package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Giao dien TicketRepository quan ly cac thao tac co so du lieu cho thuc the Ve (Ticket).
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    /**
     * Lay danh sach cac ve duoc dat boi mot khach mua (Buyer) cu the, sap xep theo ngay dat giam dan.
     *
     * @param buyerId ID cua nguoi mua ve
     * @return Danh sach ve cua khach mua sap xep tu moi nhat den cu nhat
     */
    List<Ticket> findByBuyerIdOrderByBookingDateDesc(Long buyerId);

    /**
     * Lay danh sach cac ve da duoc dat cho mot chuyen tau nhat dinh.
     *
     * @param trainId ID cua chuyen tau can lay danh sach ve
     * @return Danh sach ve thuoc chuyen tau do
     */
    List<Ticket> findByTrainId(Long trainId);

    /**
     * Lay danh sach ve da ban thuoc cac chuyen tau cua mot nguoi ban (Seller) cu the,
     * sap xep theo ngay dat giam dan de nguoi ban tien quan ly doanh thu.
     *
     * @param sellerId ID cua nguoi ban (chu so huu cac chuyen tau)
     * @return Danh sach ve da ban sap xep tu moi nhat den cu nhat
     */
    List<Ticket> findByTrainSellerIdOrderByBookingDateDesc(Long sellerId);
}

