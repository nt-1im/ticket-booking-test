package com.example.ticketbooking.model;

/**
 * Enum Role xac dinh cac vai tro/quyen han cua nguoi dung trong he thong dat ve.
 */
public enum Role {
    /** Quyen Quan tri vien: Co toan quyen quan ly tai khoan, tau va ve. */
    ROLE_ADMIN,
    
    /** Quyen Nguoi ban: Quan ly cac chuyen tau cua minh va thong ke doanh thu ban ve. */
    ROLE_SELLER,
    
    /** Quyen Nguoi mua (Khach hang): Xem danh sach chuyen tau, dat ve va quan ly lich su dat ve. */
    ROLE_BUYER
}
