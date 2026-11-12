# Kế hoạch Cải thiện & Hoàn thiện Dự án Lib-Smart (`Quanly_ThuVien`)

Tài liệu theo dõi tiến độ thực hiện qua 4 Phase theo thứ tự ưu tiên. Cập nhật trạng thái `- [x]` sau khi hoàn thành và kiểm tra từng bước.

---

## Phase 1: Chuẩn hóa Database & Cấu hình Build

### 1.1. Sửa script CSDL [`MySql_DBQLTV.sql`](MySql_DBQLTV.sql)
- [x] Xóa cụm `INSERT INTO Sach` (dòng 72–76) và `INSERT INTO NguoiDung` (dòng 80–81) bị chèn sớm trước `TacGia`/`TheLoai` và bị trùng lặp.
- [x] Giữ đúng thứ tự chèn dữ liệu mẫu: `TacGia` → `TheLoai` → `DocGia` → `Sach` → `NguoiDung` (tránh lỗi khóa ngoại khi chạy mới từ đầu).
- [ ] Kiểm tra lại toàn bộ script chạy sạch trên MySQL từ trạng thái database trống *(đã rà soát cú pháp & thứ tự khóa ngoại trong file; chưa thực thi trực tiếp vì service `MySQL80` đang dừng)*.

### 1.2. Chuẩn hóa đường dẫn thư viện trong [`nbproject/project.properties`](nbproject/project.properties)
- [x] Kiểm tra vị trí file `mysql-connector-j-26.10.0.jar` trong project (đã sao chép vào `lib/mysql-connector-j-26.10.0.jar` để không bị xóa khi clean `dist/`).
- [x] Đổi `file.reference.mysql-connector-j-26.10.0.jar` từ đường dẫn tuyệt đối (`C:\Users\Phuoc Toan\Downloads\...`) sang đường dẫn tương đối (`lib/mysql-connector-j-26.10.0.jar`), đồng thời điều chỉnh `javac.source=21` và `javac.target=21` khớp JDK 21 đang cài.
- [x] Kiểm tra build lại project bằng `javac` với classpath `lib/mysql-connector-j-26.10.0.jar` (16/16 file biên dịch thành công).

### 1.3. Chuẩn hóa tầng Model khớp với cấu trúc bảng DB
- [x] Cập nhật [`DocGia.java`](src/com/libsmart/model/DocGia.java): bỏ field `diaChi` không dùng; bổ sung các field tương ứng bảng `DocGia` (`ngayTaoThe`, `ngayHetHan`, `trangThai`).
- [x] Cập nhật [`DocGiaDAO.java`](src/com/libsmart/dao/DocGiaDAO.java) để map đầy đủ các cột `NgayTaoThe`, `NgayHetHan`, `TrangThai` khi truy vấn/cập nhật (và cập nhật lời gọi constructor trong [`QuanLyDocGiaFrame.java`](src/com/libsmart/view/QuanLyDocGiaFrame.java) để biên dịch sạch).
- [x] Rà soát [`PhieuMuon.java`](src/com/libsmart/model/PhieuMuon.java): đổi `ngayMuon` sang `java.sql.Timestamp` khớp cột `DATETIME` và `rs.getTimestamp("NgayMuon")`.

---

## Phase 2: Sửa lỗi Bảo mật, Session & Tầng DAO

### 2.1. Xử lý đăng nhập an toàn trong [`NguoiDungDAO.java`](src/com/libsmart/dao/NguoiDungDAO.java)
- [x] Bỏ fallback đăng nhập cứng `admin`/`admin` khi kết nối DB lỗi hoặc không tìm thấy tài khoản trong `checkLogin`.
- [x] Kiểm tra điều kiện `TrangThai = TRUE` khi đăng nhập và thông báo/xử lý rõ ràng khi mất kết nối CSDL (tránh trả về im lặng như sai mật khẩu).

### 2.2. Quản lý Session `NguoiDung` xuyên suốt ứng dụng
- [x] Cập nhật [`MainFrame.java`](src/com/libsmart/view/MainFrame.java) nhận đối tượng `NguoiDung` đã đăng nhập từ [`LoginFrame.java`](src/com/libsmart/view/LoginFrame.java) (hiển thị tên/vai trò thủ thư hiện tại ở thanh footer).
- [x] Truyền `NguoiDung` từ `MainFrame` sang [`QuanLyMuonTraFrame.java`](src/com/libsmart/view/QuanLyMuonTraFrame.java).
- [x] Cập nhật [`MuonTraDAO.java`](src/com/libsmart/dao/MuonTraDAO.java): dùng đúng `maNguoiDung` của thủ thư đang đăng nhập khi gọi `taoPhieuMuon(...)` thay cho `getMaNguoiDung()` lấy `LIMIT 1`.

### 2.3. Sửa lỗi nuốt ngoại lệ trong [`SachDAO.java`](src/com/libsmart/dao/SachDAO.java)
- [x] Loại bỏ các khối `catch (SQLException e) {}` rỗng ở toàn bộ các phương thức (`getAllSach`, `searchSachByTen`, `getMapTacGia`, `getMapTheLoai`, `insertSach`, `updateSach`, `deleteSach`).
- [x] Ghi log lỗi qua `Logger` và ném `SQLException` lên tầng UI để xử lý và thông báo rõ ràng.

### 2.4. Viết truy vấn thống kê thực tế cho Dashboard
- [x] Bổ sung phương thức `countActiveDocGia()` đếm số độc giả đang hoạt động (`TrangThai = 'HOAT_DONG'`) trong [`DocGiaDAO.java`](src/com/libsmart/dao/DocGiaDAO.java).
- [x] Bổ sung phương thức `countPhieuDangMuon()` đếm số phiếu đang cho mượn (`TrangThai = 'DANG_MUON'`) trong [`MuonTraDAO.java`](src/com/libsmart/dao/MuonTraDAO.java).
- [x] Bổ sung `countTongDauSach()` và `sumTongSoLuongSach()` trong [`SachDAO.java`](src/com/libsmart/dao/SachDAO.java) để hiển thị chính xác trên Stat Card ở Phase 3.

---

## Phase 3: Đồng bộ Dữ liệu UI & Hoàn thiện Nghiệp vụ

### 3.1. Đồng bộ dữ liệu động trên [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java)
- [x] Nạp dữ liệu động vào `cbTacGia` và `cbTheLoai` từ `SachDAO.getMapTacGia()` và `SachDAO.getMapTheLoai()` thay cho danh sách hardcode.
- [x] Khi click chọn một dòng trên `JTable`, tự động chọn đúng item tương ứng trong `cbTacGia` và `cbTheLoai` theo `MaTacGia` và `MaTheLoai`.
- [x] Hiển thị tên Tác giả / Thể loại rõ ràng trên bảng và đồng bộ với ComboBox.

### 3.2. Đồng bộ dữ liệu & tự động refresh trên [`MainFrame.java`](src/com/libsmart/view/MainFrame.java)
- [x] Gắn số liệu thực từ DAO vào 3 Stat Card (`TỔNG SỐ SÁCH`, `ĐỘC GIẢ TÍCH CỰC`, `ĐANG CHO MƯỢN`).
- [x] Viết hàm `refreshDashboard()` cập nhật lại cả 3 Stat Card và bảng tổng quan sách mới cập nhật.
- [x] Gắn listener (`WindowAdapter` khi đóng cửa sổ con + callback `onDataChanged` khi thêm/sửa/xóa/mượn/trả từ `QuanLySachFrame`, `QuanLyDocGiaFrame`, `QuanLyMuonTraFrame`) để `MainFrame` tự động refresh dữ liệu mới nhất.

### 3.3. Khóa chỉnh sửa trực tiếp trên các `JTable`
- [x] Override `isCellEditable(int row, int column) { return false; }` cho `DefaultTableModel` tại [`MainFrame.java`](src/com/libsmart/view/MainFrame.java).
- [x] Override `isCellEditable` trả về `false` tại [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java).
- [x] Override `isCellEditable` trả về `false` tại [`QuanLyDocGiaFrame.java`](src/com/libsmart/view/QuanLyDocGiaFrame.java).
- [x] Override `isCellEditable` trả về `false` tại [`QuanLyMuonTraFrame.java`](src/com/libsmart/view/QuanLyMuonTraFrame.java).

### 3.4. Hoàn thiện nghiệp vụ Mượn – Trả & Thẻ Độc giả
- [x] Khi lập phiếu mượn (`MuonTraDAO.taoPhieuMuonChiTiet` / `QuanLyMuonTraFrame`): kiểm tra độc giả có `TrangThai = 'HOAT_DONG'` và thẻ còn hạn (`NgayHetHan >= CURRENT_DATE`), chặn mượn và thông báo rõ nếu thẻ hết hạn/bị khóa.
- [x] Khi trả sách (`MuonTraDAO.traSachChiTiet`): so sánh `CURRENT_DATE` với `NgayHenTra`; nếu quá hạn, tự động tính `TienPhat` (`5.000 VNĐ/ngày`) và cập nhật `TienPhat`, `GhiChu` vào `ChiTietPhieuMuon`.
- [x] Hiển thị thông báo kết quả trả sách kèm số ngày trễ và tiền phạt (nếu có) trên [`QuanLyMuonTraFrame.java`](src/com/libsmart/view/QuanLyMuonTraFrame.java) và cột `Tiền Phạt` trên bảng.

---

## Phase 4: Nâng cấp UI/UX

### 4.1. Chuẩn hóa giao diện & trải nghiệm người dùng trên toàn bộ Frame
- [x] Đồng bộ bảng màu, font chữ, kích thước nút/input, khoảng cách padding và trạng thái hover/focus thông qua [`UITheme.java`](src/com/libsmart/utils/UITheme.java) trên [`LoginFrame.java`](src/com/libsmart/view/LoginFrame.java), [`MainFrame.java`](src/com/libsmart/view/MainFrame.java), [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java), [`QuanLyDocGiaFrame.java`](src/com/libsmart/view/QuanLyDocGiaFrame.java), [`QuanLyMuonTraFrame.java`](src/com/libsmart/view/QuanLyMuonTraFrame.java).
- [x] Cải thiện bố cục bảng dữ liệu (độ rộng cột, căn lề, zebra striping, định dạng tiền VNĐ, màu trạng thái Đang mượn / Đã trả / Quá hạn / Hoạt động / Hết hạn, hiển thị thẻ phiên làm việc ở Sidebar Dashboard).
- [ ] Kiểm tra luồng thao tác đầu-cuối (đăng nhập → quản lý sách/độc giả → mượn/trả → đăng xuất) sau khi hoàn thiện UI *(chưa chạy kiểm thử tương tác trực tiếp với MySQL server do service `MySQL80` đang dừng — đã biên dịch sạch 17/17 file nguồn Java)*.
