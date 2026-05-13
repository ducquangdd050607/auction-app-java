package com.auctionapp.auctionappjava.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneyUtils — xử lý chuỗi tiền tệ")
class MoneyUtilsTest {

    // ================================================================ purifyingText
    @Test
    @DisplayName("purifyingText — số thường không có dấu")
    void purifyingText_withPlainNumber_shouldParse() {
        BigDecimal result = MoneyUtils.purifyingText("50000");
        assertEquals(new BigDecimal("50000"), result);
    }

    @Test
    @DisplayName("purifyingText — số có dấu chấm phân cách kiểu VN (1.000.000)")
    void purifyingText_withDotSeparators_shouldStripAndParse() {
        BigDecimal result = MoneyUtils.purifyingText("1.000.000");
        assertEquals(new BigDecimal("1000000"), result);
    }

    @Test
    @DisplayName("purifyingText — số có dấu phẩy phân cách kiểu Mỹ (1,000,000)")
    void purifyingText_withCommaSeparators_shouldStripAndParse() {
        BigDecimal result = MoneyUtils.purifyingText("1,000,000");
        assertEquals(new BigDecimal("1000000"), result);
    }

    @Test
    @DisplayName("purifyingText — input null phải trả về ZERO, không ném exception")
    void purifyingText_withNull_shouldReturnZero() {
        BigDecimal result = MoneyUtils.purifyingText(null);
        assertEquals(BigDecimal.ZERO, result, "null phải trả về BigDecimal.ZERO");
    }

    @Test
    @DisplayName("purifyingText — input rỗng phải trả về ZERO")
    void purifyingText_withEmpty_shouldReturnZero() {
        BigDecimal result = MoneyUtils.purifyingText("");
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("purifyingText — chỉ toàn chữ cái phải trả về ZERO")
    void purifyingText_withLettersOnly_shouldReturnZero() {
        BigDecimal result = MoneyUtils.purifyingText("abc");
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("purifyingText — chuỗi hỗn hợp chữ và số (ví dụ: '10k') chỉ lấy số")
    void purifyingText_withMixedChars_shouldExtractDigitsOnly() {
        BigDecimal result = MoneyUtils.purifyingText("500.000 VND");
        assertEquals(new BigDecimal("500000"), result);
    }

    @Test
    @DisplayName("purifyingText — chuỗi chỉ khoảng trắng phải trả về ZERO")
    void purifyingText_withBlankString_shouldReturnZero() {
        BigDecimal result = MoneyUtils.purifyingText("   ");
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("purifyingText — số 0 phải trả về ZERO chính xác")
    void purifyingText_withZero_shouldReturnZero() {
        BigDecimal result = MoneyUtils.purifyingText("0");
        assertEquals(new BigDecimal("0"), result);
    }

    @Test
    @DisplayName("purifyingText — số lớn (long max) không được crash")
    void purifyingText_withLargeNumber_shouldHandleWithoutCrash() {
        // Chỉ cần không ném exception
        assertDoesNotThrow(() -> MoneyUtils.purifyingText("999999999999999"));
    }

    // ================================================================ zeroIfNull
    @Test
    @DisplayName("zeroIfNull — với null phải trả về BigDecimal.ZERO")
    void zeroIfNull_withNull_shouldReturnZero() {
        assertEquals(BigDecimal.ZERO, MoneyUtils.zeroIfNull(null));
    }

    @Test
    @DisplayName("zeroIfNull — với giá trị hợp lệ phải trả về đúng giá trị đó")
    void zeroIfNull_withValue_shouldReturnSameValue() {
        BigDecimal value = new BigDecimal("999999");
        assertSame(value, MoneyUtils.zeroIfNull(value));
    }

    @Test
    @DisplayName("zeroIfNull — với ZERO phải trả về chính ZERO")
    void zeroIfNull_withZero_shouldReturnZero() {
        assertEquals(BigDecimal.ZERO, MoneyUtils.zeroIfNull(BigDecimal.ZERO));
    }
}