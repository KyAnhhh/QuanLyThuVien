package com.libsmart.view;

/**
 * Giao diện chuẩn cho các màn hình con dạng JPanel trong kiến trúc Single-Page CardLayout.
 */
public interface RefreshableView {

    /**
     * Nạp lại dữ liệu mới nhất từ CSDL mỗi khi màn hình được chuyển tới.
     */
    void refreshData();
}
