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

/**
 * Controller xu ly cac yeu cau trang chu cong khai (Home Page) va hien thi
 * thong tin chuyen tau.
 */
@Controller
public class HomeController {

    private final TrainService trainService;
    private final com.example.ticketbooking.service.TicketService ticketService;

    /**
     * Ham khoi dung HomeController.
     *
     * @param trainService  Dich vu quan ly chuyen tau
     * @param ticketService Dich vu quan ly ve tau
     */
    @Autowired
    public HomeController(TrainService trainService, com.example.ticketbooking.service.TicketService ticketService) {
        this.trainService = trainService;
        this.ticketService = ticketService;
    }

    /**
     * Hien thi trang chu va ho tro tim kiem dong cac chuyen tau theo ga di, ga den
     * va ngay khoi hanh.
     *
     * @param departure Ga di (Khong bat buoc)
     * @param arrival   Ga den (Khong bat buoc)
     * @param date      Ngay khoi hanh chuyen tau (Khong bat buoc)
     * @param model     Doi tuong chua du lieu de truyen sang Thymeleaf template
     * @return Ten template hien thi trang chu ("home")
     */
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
            model.addAttribute("isSearch", false);
        }
        model.addAttribute("trains", trains);
        return "home";
    }

    /**
     * Hien thi trang chi tiet cua mot chuyen tau cu the.
     *
     * @param id    ID cua chuyen tau can xem
     * @param model Doi tuong chua du lieu de truyen sang Thymeleaf template
     * @return Ten template hien thi chi tiet chuyen tau ("train/detail")
     * @throws IllegalArgumentException Nem ra khi ID chuyen tau khong hop le hoac
     *                                  khong ton tai
     */
    @GetMapping("/trains/detail/{id}")
    public String trainDetail(@PathVariable("id") Long id, Model model) {
        Train train = trainService.getTrainById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid train ID: " + id));
        model.addAttribute("train", train);
        model.addAttribute("occupiedSeats", ticketService.getOccupiedSeats(id));
        return "train/detail";
    }
}
