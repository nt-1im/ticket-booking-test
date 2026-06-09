package com.example.ticketbooking.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Lop tu van Controller toan cuc (Global Controller Advice).
 * Cung cap du lieu dung chung cho toan bo cac Controller va trang giao dien (view).
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Tu dong them thuoc tinh "requestURI" vao tat ca cac Model cua Controller.
     * Thuoc tinh nay chua URI yeu cau hien tai, giup cac trang giao dien (nhu menu dieu huong) xac dinh active tab.
     *
     * @param request Doi tuong HttpServletRequest chua thong tin yeu cau HTTP hien tai
     * @return Chuoi URI cua yeu cau hien tai (vi du: /dashboard, /trains)
     */
    @ModelAttribute("requestURI")
    public String getRequestServletPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
