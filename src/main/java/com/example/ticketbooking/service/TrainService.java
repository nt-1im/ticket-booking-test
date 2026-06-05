package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    private final TrainRepository trainRepository;

    @Autowired
    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    public List<Train> getAllTrains() {
        return trainRepository.findAllByOrderByDepartureDateAscDepartureTimeAsc();
    }

    public List<Train> getTrainsBySeller(Long sellerId) {
        return trainRepository.findBySellerId(sellerId);
    }

    public Optional<Train> getTrainById(Long id) {
        return trainRepository.findById(id);
    }

    @Transactional
    public Train saveTrain(Train train, User seller) {
        train.setSeller(seller);
        if (train.getId() == null) {
            train.setAvailableSeats(train.getTotalSeats());
        }
        return trainRepository.save(train);
    }

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

    @Transactional
    public void deleteTrain(Long id) {
        trainRepository.deleteById(id);
    }

    public List<Train> searchTrains(String departure, String arrival, LocalDate date) {
        boolean hasDeparture = departure != null && !departure.isBlank();
        boolean hasArrival = arrival != null && !arrival.isBlank();
        
        if (hasDeparture && hasArrival && date != null) {
            return trainRepository.findByDepartureStationContainingIgnoreCaseAndArrivalStationContainingIgnoreCaseAndDepartureDate(
                    departure, arrival, date);
        }
        return getAllTrains();
    }
}
