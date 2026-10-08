package org.example.shopkeeper_backend.common;

import java.util.regex.Pattern;

/**
 * 联系电话校验：11位手机号 或 带区号固话；null/空白视为不填，放行
 */
public final class PhoneValidator {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$");

    private PhoneValidator() {
    }

    public static void check(String phone) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        // strip() 容忍录入时首尾误带的空格
        if (!PHONE_PATTERN.matcher(phone.strip()).matches()) {
            throw new BusinessException("联系电话格式不正确，请输入11位手机号或带区号的固话");
        }
    }

    /**
     * 落库前归一：去首尾空格；空串/空白归为 null（字段可空，表示不填）
     */
    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String stripped = phone.strip();
        return stripped.isEmpty() ? null : stripped;
    }
}
