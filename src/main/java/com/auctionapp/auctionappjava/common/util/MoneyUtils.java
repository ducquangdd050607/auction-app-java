package com.auctionapp.auctionappjava.common.util;

import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {

    private MoneyUtils() {}

    public static BigDecimal zeroIfNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public static void settingMoneyFormat(TextField txtAmount) {
    // Ép hệ thống dùng chuẩn Mỹ để chắc chắn hàng nghìn được ngăn cách bằng dấu phẩy (,)
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###");

        // Lắng nghe MỌI SỰ THAY ĐỔI từng phím gõ vào ô txtAmount
        txtAmount.textProperty().addListener((observable, oldValue, newValue) -> {

            // Nếu xóa trắng ô thì không làm gì cả
            if (newValue == null || newValue.isEmpty()) {
                return;
            }

            // 1. Tẩy sạch mọi ký tự không phải là số (Chặn luôn người dùng gõ chữ)
            String cleanInput = newValue.replaceAll("[^\\d]", "");

            try {
                if (cleanInput.isEmpty()) {
                    txtAmount.setText("");
                    return;
                }

                // 2. Ép sang số và định dạng (VD: 100000 -> "100,000")
                long amount = Long.parseLong(cleanInput);
                String formattedStr = formatter.format(amount);

                // 3. Chuẩn hóa sang kiểu Việt Nam (Đổi dấu phẩy thành dấu chấm -> "100.000")
                formattedStr = formattedStr.replaceAll(",", ".");

                // 4. CHỐNG VÒNG LẶP VÔ TẬN: Chỉ cập nhật khi chuỗi mới thực sự khác
                if (!newValue.equals(formattedStr)) {

                    // 5. CHỐNG NHẢY CON TRỎ CHUỘT: Tính toán độ chênh lệch chiều dài chuỗi
                    int cursorPosition = txtAmount.getCaretPosition();
                    int lengthDiff = formattedStr.length() - newValue.length();

                    // Ghi đè text mới lên giao diện
                    txtAmount.setText(formattedStr);

                    // Đẩy con trỏ chuột về đúng vị trí nó đang đứng
                    txtAmount.positionCaret(cursorPosition + lengthDiff);
                }

            } catch (NumberFormatException e) {
                // TODO: xử lí exception số quá lớn
                // Nếu người dùng cố tình dán (paste) một số quá lớn vượt ngưỡng long
                // Ép quay về giá trị cũ trước khi dán
                txtAmount.setText(oldValue);
            }
        });
    }

    public static long purifyingText(String price) {
        return Long.parseLong(price.replaceAll("[^\\d]", ""));
    }

}