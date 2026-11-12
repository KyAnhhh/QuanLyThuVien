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

---

## Phase 5: Hoàn thiện Vận hành Thư viện

### 5.1. Quản lý Tác giả & Thể loại
- [x] Bổ sung CRUD cho `TacGia` và `TheLoai` trong [`SachDAO.java`](src/com/libsmart/dao/SachDAO.java) (thêm, sửa tên, xóa có kiểm tra ràng buộc khóa ngoại).
- [x] Tạo hộp thoại quản lý danh mục [`DanhMucDialog.java`](src/com/libsmart/view/DanhMucDialog.java) và gắn nút thêm/quản lý nhanh ngay bên cạnh `cbTacGia`, `cbTheLoai` trên [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java).
- [x] Tự động nạp lại ComboBox và chọn đúng Tác giả / Thể loại vừa thêm trên [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java).

### 5.2. Gia hạn & Khóa/Mở khóa thẻ Độc giả
- [x] Chuẩn hóa xử lý lỗi trong [`DocGiaDAO.java`](src/com/libsmart/dao/DocGiaDAO.java) (log `Logger` và ném `SQLException` thay vì nuốt lỗi).
- [x] Bổ sung hàm `giaHanThe(maDocGia, soNam)` và `capNhatTrangThai(maDocGia, trangThai)` trong [`DocGiaDAO.java`](src/com/libsmart/dao/DocGiaDAO.java).
- [x] Cập nhật [`QuanLyDocGiaFrame.java`](src/com/libsmart/view/QuanLyDocGiaFrame.java): thêm ô `Ngày Hết Hạn`, `Trạng Thái` (`HOAT_DONG` / `KHOA`) cùng nút thao tác nhanh **Gia hạn +1 năm** và **Khóa / Mở khóa**.

### 5.3. Validate dữ liệu & Thông báo lỗi chi tiết
- [x] Tạo tiện ích kiểm tra dữ liệu và diễn giải lỗi SQL thân thiện [`ValidationUtils.java`](src/com/libsmart/utils/ValidationUtils.java) (trùng khóa `UNIQUE`, vướng khóa ngoại `FOREIGN KEY`, SĐT, Email, ngày `yyyy-MM-dd`).
- [x] Áp dụng validate đầy đủ trên [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java) (năm xuất bản, `0 <= SoLuongCon <= SoLuongTong`, giá bìa `>= 0`) và [`QuanLyDocGiaFrame.java`](src/com/libsmart/view/QuanLyDocGiaFrame.java) (họ tên, SĐT, Email, ngày hết hạn).

---

## Phase 6: Nâng cấp Nghiệp vụ Mượn – Trả & Tìm kiếm

### 6.1. Mượn nhiều sách trong một phiếu
- [x] Cho phép thêm nhiều sách vào giỏ sách tạm trên [`QuanLyMuonTraFrame.java`](src/com/libsmart/view/QuanLyMuonTraFrame.java) và lưu nhiều dòng `ChiTietPhieuMuon` trong cùng một transaction qua [`MuonTraDAO.taoPhieuMuonNhieuSach`](src/com/libsmart/dao/MuonTraDAO.java).
- [x] Hỗ trợ trả từng cuốn (`traTheoChiTiet`) hoặc trả cả phiếu (`traSachChiTiet`) an toàn, tự động chốt `PhieuMuon.TrangThai = 'DA_TRA'` khi đã trả hết.

### 6.2. Quy định hạn mức mượn & Ràng buộc quá hạn
- [x] Giới hạn số lượng sách đang mượn tối đa của một độc giả (`MAX_SACH_DANG_MUON = 5`).
- [x] Chặn lập phiếu mượn mới nếu độc giả đang có phiếu quá hạn chưa trả (`countPhieuQuaHanCuaDocGia`).

### 6.3. Bộ lọc & Tra cứu nâng cao
- [x] Thêm bộ lọc trạng thái (`Tất cả`, `Đang mượn`, `Quá hạn`, `Đã trả`) trên [`QuanLyMuonTraFrame.java`](src/com/libsmart/view/QuanLyMuonTraFrame.java) và hiển thị cột `Mã CT`, `Thủ Thư`, `Ghi Chú`.
- [x] Thêm bộ lọc Tác giả / Thể loại / Chỉ sách còn hàng trên [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java).

---

## Phase 7: Cấu hình, Bảo mật & Phân quyền

### 7.1. Tách cấu hình kết nối CSDL
- [x] Đọc thông số JDBC (URL, user, password) từ `config.properties` (có fallback mặc định và file mẫu [`config.properties.example`](config.properties.example)) trong [`DBConnection.java`](src/com/libsmart/utils/DBConnection.java).

### 7.2. Mã hóa mật khẩu & Đổi mật khẩu
- [x] Tạo [`PasswordUtils.java`](src/com/libsmart/utils/PasswordUtils.java) (`PBKDF2WithHmacSHA256` + salt), băm mật khẩu khi lưu và tự động nâng cấp mật khẩu cũ khi đăng nhập trong [`NguoiDungDAO.java`](src/com/libsmart/dao/NguoiDungDAO.java).
- [x] Thêm hộp thoại [`DoiMatKhauDialog.java`](src/com/libsmart/view/DoiMatKhauDialog.java) và nút **Đổi Mật Khẩu** trên [`MainFrame.java`](src/com/libsmart/view/MainFrame.java).

### 7.3. Quản trị Tài khoản & Phân quyền
- [x] Thêm màn hình [`QuanLyTaiKhoanFrame.java`](src/com/libsmart/view/QuanLyTaiKhoanFrame.java) (thêm thủ thư, đổi vai trò `ADMIN` / `THU_THU`, đặt lại mật khẩu, khóa/mở tài khoản) chỉ hiển thị với tài khoản `ADMIN` trên [`MainFrame.java`](src/com/libsmart/view/MainFrame.java) (và cập nhật tài khoản mẫu `ADMIN` + `THU_THU` trong [`MySql_DBQLTV.sql`](MySql_DBQLTV.sql)).

---

## Phase 8: Thống kê Báo cáo & Kiểm thử

### 8.1. Màn hình Thống kê & Báo cáo
- [x] Tạo [`ThongKeDAO.java`](src/com/libsmart/dao/ThongKeDAO.java) và [`ThongKeFrame.java`](src/com/libsmart/view/ThongKeFrame.java) (gắn nút **Thống Kê - Báo Cáo** trên [`MainFrame.java`](src/com/libsmart/view/MainFrame.java)) thống kê danh sách phiếu quá hạn kèm SĐT/Email liên hệ, tổng tiền phạt theo tháng/năm và Top 20 sách mượn nhiều nhất.

### 8.2. Xuất dữ liệu CSV
- [x] Tạo [`CsvExporter.java`](src/com/libsmart/utils/CsvExporter.java) xuất file `.csv` chuẩn UTF-8 BOM (mở đúng tiếng Việt trên Excel) và gắn nút **Xuất CSV** trên [`QuanLySachFrame.java`](src/com/libsmart/view/QuanLySachFrame.java), [`QuanLyDocGiaFrame.java`](src/com/libsmart/view/QuanLyDocGiaFrame.java), [`QuanLyMuonTraFrame.java`](src/com/libsmart/view/QuanLyMuonTraFrame.java) và 3 tab của [`ThongKeFrame.java`](src/com/libsmart/view/ThongKeFrame.java).

### 8.3. Kiểm thử tự động (`test/`)
- [x] Viết bộ unit test [`LibraryLogicTest.java`](test/com/libsmart/LibraryLogicTest.java) kiểm thử validate dữ liệu, mã hóa/kiểm tra mật khẩu, hạn thẻ độc giả, tính tiền phạt quá hạn và xuất CSV (`32/32` test pass).
