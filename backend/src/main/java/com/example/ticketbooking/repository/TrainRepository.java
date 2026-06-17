package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.Train;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Giao dien TrainRepository cung cap cac phuong thuc truy xuat du lieu tu bang Chuyen tau (Train).
 * Tich hop khoa bi quan (Pessimistic Locking) va tim kiem dong.
 */
@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
        
        /**
         * Tim danh sach cac chuyen tau duoc tao boi mot nguoi ban (Seller) cu the.
         *
         * @param sellerId ID cua nguoi ban
         * @return Danh sach chuyen tau do nguoi ban do quan ly
         */
        List<Train> findBySellerId(Long sellerId);

        /**
         * Tim cac chuyen tau theo ga di, ga den (khong phan biet hoa thuong) va ngay khoi hanh chinh xac.
         *
         * @param departureStation Ga di (tim kiem tuong doi)
         * @param arrivalStation Ga den (tim kiem tuong doi)
         * @param departureDate Ngay di chinh xac
         * @return Danh sach cac chuyen tau thoa man dieu kien
         */
        List<Train> findByRouteDepartureStationContainingIgnoreCaseAndRouteArrivalStationContainingIgnoreCaseAndDepartureDate(
                        String departureStation, String arrivalStation, LocalDate departureDate);

        /**
         * Lay toan bo danh sach chuyen tau, sap xep tang dan theo ngay khoi hanh va gio khoi hanh.
         *
         * @return Danh sach cac chuyen tau duoc sap xep theo thoi gian tang dan
         */
        List<Train> findAllByOrderByDepartureDateAscDepartureTimeAsc();

        /**
         * Tim chuyen tau theo ID va ap dung khoa ghi bi quan (Pessimistic Write Lock).
         * Giup ngan chan xung dot du lieu (Race Condition) khi nhieu luong cung thuc hien mua ve.
         *
         * @param id ID cua chuyen tau can tim va khoa
         * @return Mot Optional chua thong tin Train neu tim thay
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select t from Train t where t.id = :id")
        Optional<Train> findByIdForUpdate(@Param("id") Long id);

        /**
         * Tim kiem dong cac chuyen tau theo ga di, ga den va ngay di.
         * Cho pheps cac tham so tim kiem co the mang gia tri null hoac rong.
         *
         * @param departure Ga di (neu null hoac rong thi bo qua dieu kien nay)
         * @param arrival Ga den (neu null hoac rong thi bo qua dieu kien nay)
         * @param date Ngay khoi hanh (neu null thi bo qua dieu kien nay)
         * @return Danh sach chuyen tau phu hop voi bo loc tim kiem dong
         */
        @Query("SELECT t FROM Train t JOIN t.route r WHERE " +
                        "(:departure IS NULL OR :departure = '' OR LOWER(r.departureStation) LIKE LOWER(CONCAT('%', :departure, '%'))) AND "
                        +
                        "(:arrival IS NULL OR :arrival = '' OR LOWER(r.arrivalStation) LIKE LOWER(CONCAT('%', :arrival, '%'))) AND "
                        +
                        "(:date IS NULL OR t.departureDate = :date) " +
                        "ORDER BY t.departureDate ASC, t.departureTime ASC")
        List<Train> searchTrainsDynamic(
                        @Param("departure") String departure,
                        @Param("arrival") String arrival,
                        @Param("date") LocalDate date);
}
