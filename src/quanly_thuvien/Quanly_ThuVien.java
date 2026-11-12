
package quanly_thuvien;

import com.libsmart.view.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Quanly_ThuVien {

    public static void main(String[] args) {

        // Thiết lập giao diện theo hệ điều hành
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Khởi chạy giao diện đăng nhập
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }
}
