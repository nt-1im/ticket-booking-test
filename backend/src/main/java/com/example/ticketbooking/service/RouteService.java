package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Route;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Dich vu quan ly tuyen duong co dinh (Route).
 */
@Service
public class RouteService {

    private final RouteRepository routeRepository;

    @Autowired
    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<Route> getRoutesBySeller(Long sellerId) {
        return routeRepository.findBySellerId(sellerId);
    }

    public Optional<Route> getRouteById(Long id) {
        return routeRepository.findById(id);
    }

    @Transactional
    public Route saveRoute(Route route, User seller) {
        route.setSeller(seller);
        return routeRepository.save(route);
    }

    @Transactional
    public Route getOrCreateRoute(String departureStation, String arrivalStation, User seller) {
        return routeRepository.findByDepartureStationIgnoreCaseAndArrivalStationIgnoreCaseAndSellerId(
                departureStation, arrivalStation, seller.getId())
                .orElseGet(() -> {
                    Route route = new Route(departureStation, arrivalStation, seller);
                    return routeRepository.save(route);
                });
    }

    @Transactional
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
}
