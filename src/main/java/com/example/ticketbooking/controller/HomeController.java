package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final TrainService trainService;

    @Autowired
    public HomeController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(value = "departure", required = false) String departure,
            @RequestParam(value = "arrival", required = false) String arrival,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        
        List<Train> trains;
        if ((departure != null && !departure.isBlank()) || (arrival != null && !arrival.isBlank()) || date != null) {
            trains = trainService.searchTrains(departure, arrival, date);
            model.addAttribute("departure", departure);
            model.addAttribute("arrival", arrival);
            model.addAttribute("date", date);
            model.addAttribute("isSearch", true);
        } else {
            trains = trainService.getAllTrains();
        }
        model.addAttribute("trains", trains);
        return "home";
    }

    @GetMapping("/trains/detail/{id}")
    public String trainDetail(@PathVariable("id") Long id, Model model) {
        Train train = trainService.getTrainById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid train ID: " + id));
        model.addAttribute("train", train);
        return "train/detail";
    }
}

