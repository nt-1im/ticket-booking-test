package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.TicketRepository;
import com.example.ticketbooking.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Dich vu xu ly cac nghiep vu lien quan den chuyen tau (Train).
 * Bao gom tim kiem, them moi, cap nhat ghe ngoi, va xoa chuyen tau.
 */
@Service
public class TrainService {

    private final TrainRepository trainRepository;
    private final TicketRepository ticketRepository;

    /**
     * Ham khoi dung TrainService.
     *
     * @param trainRepository Repository quan ly chuyen tau
     * @param ticketRepository Repository quan ly ve tau
     */
    @Autowired
    public TrainService(TrainRepository trainRepository, TicketRepository ticketRepository) {
        this.trainRepository = trainRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Lay toan bo chuyen tau trong co so du lieu, duoc sap xep tang dan theo thoi gian di.
     *
     * @return Danh sach chuyen tau sap xep tang dan theo ngay/gio di
     */
    public List<Train> getAllTrains() {
        return trainRepository.findAllByOrderByDepartureDateAscDepartureTimeAsc();
    }

    /**
     * Lay danh sach chuyen tau thuoc so huu cua mot nguoi ban (Seller) nhat dinh.
     *
     * @param sellerId ID cua nguoi ban
     * @return Danh sach chuyen tau cua nguoi ban
     */
    public List<Train> getTrainsBySeller(Long sellerId) {
        return trainRepository.findBySellerId(sellerId);
    }

    /**
     * Tim chuyen tau theo ID.
     *
     * @param id ID cua chuyen tau can tim
     * @return Mot Optional chua chuyen tau neu tim thay
     */
    public Optional<Train> getTrainById(Long id) {
        return trainRepository.findById(id);
    }

    /**
     * Luu thong tin chuyen tau moi hoac cap nhat thong tin nguoi ban cho chuyen tau.
     * Neu la chuyen tau moi hoan toan, khoi tao so ghe trong bang tong so ghe.
     *
     * @param train Doi tuong chuyen tau can luu
     * @param seller Nguoi ban chiu trach nhiem chuyen tau nay
     * @return Doi tuong chuyen tau sau khi luu thanh cong
     */
    @Transactional
    public Train saveTrain(Train train, User seller) {
        train.setSeller(seller);
        if (train.getId() == null) {
            train.setAvailableSeats(train.getTotalSeats());
        }
        return trainRepository.save(train);
    }

    /**
     * Cap nhat thong tin chi tiet chuyen tau hien tai.
     * Ho tro tinh toan lai so ghe trong khi thay doi tong so luong ghe.
     *
     * @param trainId ID cua chuyen tau can cap nhat
     * @param updatedTrain Doi tuong chua thong tin moi de cap nhat
     * @return Chuyen tau sau khi da cap nhat thanh cong
     * @throws IllegalArgumentException Nem ra khi khong tim thay tau hoac khi thay doi lam giam so ghe trong xuong duoi 0
     */
    @Transactional
    public Train updateTrain(Long trainId, Train updatedTrain) {
        Train existingTrain = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found with ID: " + trainId));

        existingTrain.setTrainNumber(updatedTrain.getTrainNumber());
        existingTrain.setDepartureStation(updatedTrain.getDepartureStation());
        existingTrain.setArrivalStation(updatedTrain.getArrivalStation());
        existingTrain.setDepartureDate(updatedTrain.getDepartureDate());
        existingTrain.setDepartureTime(updatedTrain.getDepartureTime());
        existingTrain.setPrice(updatedTrain.getPrice());

        int diffSeats = updatedTrain.getTotalSeats() - existingTrain.getTotalSeats();
        int newAvailableSeats = existingTrain.getAvailableSeats() + diffSeats;

        if (newAvailableSeats < 0) {
            throw new IllegalArgumentException("Cannot reduce capacity below already booked seat counts!");
        }

        existingTrain.setTotalSeats(updatedTrain.getTotalSeats());
        existingTrain.setAvailableSeats(newAvailableSeats);

        return trainRepository.save(existingTrain);
    }

    /**
     * Xoa mot chuyen tau khoi co so du lieu dua tren ID.
     * Chi cho phep xoa khi chuyen tau chua co bat ky giao dich dat ve nao lien ket.
     *
     * @param id ID cua chuyen tau can xoa
     * @throws IllegalArgumentException Nem ra khi da co ve lien ket voi chuyen tau nay
     */
    @Transactional
    public void deleteTrain(Long id) {
        if (!ticketRepository.findByTrainId(id).isEmpty()) {
            throw new IllegalArgumentException("Khong the xoa chuyen tau vi da co ve duoc dat/lien ket.");
        }
        trainRepository.deleteById(id);
    }

    /**
     * Tim kiem dong cac chuyen tau phu hop voi ga di, ga den va ngay di duoc chi dinh.
     *
     * @param departure Ten ga di (tuong doi)
     * @param arrival Ten ga den (tuong doi)
     * @param date Ngay khoi hanh chuyen tau
     * @return Danh sach chuyen tau phu hop bo loc tim kiem
     */
    public List<Train> searchTrains(String departure, String arrival, LocalDate date) {
        return trainRepository.searchTrainsDynamic(departure, arrival, date);
    }
}
