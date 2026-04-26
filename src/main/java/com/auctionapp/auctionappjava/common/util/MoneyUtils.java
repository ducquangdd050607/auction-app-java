package com.auctionapp.auctionappjava.common.util;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

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

        // Chuyển sang sử dụng TEXTFORMATTER
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            // Nếu chỉ là click chuột, bôi đen (không đổi text) thì cho qua
            if (!change.isContentChange()) {
                return change;
            }

            // Lấy chuỗi mà người dùng định tạo ra (chưa được in lên UI)
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            // Đếm số lượng chữ số nằm trước con trỏ trong chuỗi
            int caretPos = change.getCaretPosition();
            int digitsBeforeCaret = 0;
            for (int i = 0; i < caretPos && i < newText.length(); i++) {
                if (Character.isDigit(newText.charAt(i))) {
                    digitsBeforeCaret++;
                }
            }

            // Lọc bỏ mọi thứ không phải là số
            String cleanText = newText.replaceAll("[^\\d]", "");

            if (cleanText.isEmpty()) {
                // Không cho gõ chữ cái vào
                return null;
            }

            try {
                // Format tiền tệ và GHI ĐÈ toàn bộ thao tác của người dùng
                long amount = Long.parseLong(cleanText);
                String formattedStr = formatter.format(amount).replaceAll(",", ".");

                // Ép sự thay đổi này áp dụng cho TOÀN BỘ chiều dài của TextField
                change.setRange(0, change.getControlText().length());

                // Nhét chuỗi đã format đẹp đẽ vào
                change.setText(formattedStr);

                // Dò lại đúng vị trí con trỏ an toàn tuyệt đối
                int newCaretPos = 0;
                int digitsCounted = 0;
                for (int i = 0; i < formattedStr.length(); i++) {
                    if (digitsCounted == digitsBeforeCaret) {
                        break;
                    }
                    if (Character.isDigit(formattedStr.charAt(i))) {
                        digitsCounted++;
                    }
                    newCaretPos++;
                }

                // Cập nhật vị trí con trỏ và mốc bôi đen ngay bên trong gói tin
                change.setCaretPosition(newCaretPos);
                change.setAnchor(newCaretPos);

                // CHẤP NHẬN gói tin đã được nhào nặn này
                return change;

            } catch (NumberFormatException e) {
                // Nếu số nhập vào to vượt quá kiểu 'long', TỪ CHỐI không cho nhập thêm
                return null;
            }
        });

        // 3. GẮN VÀO TEXTFIELD
        txtAmount.setTextFormatter(textFormatter);
    }

    public static BigDecimal purifyingText(String price) {
        // 1. Kiểm tra nếu chuỗi rỗng hoặc null để tránh lỗi crash
        if (price == null || price.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 2. Làm sạch chuỗi (chỉ giữ lại số)
        String cleanString = price.replaceAll("[^\\d]", "");

        // 3. Chuyển đổi sang BigDecimal
        // Nếu chuỗi sau khi làm sạch bị trống (ví dụ input chỉ toàn chữ), trả về 0
        if (cleanString.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(cleanString);
    }
}