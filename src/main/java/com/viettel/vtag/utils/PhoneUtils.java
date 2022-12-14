package com.viettel.vtag.utils;

import com.viettel.vtag.model.entity.User;

public class PhoneUtils {

    private PhoneUtils() { }

    public static String standardize(User user) {
        var phone = standardize(user.phone());
        user.phone(phone);
        return phone;
    }

    public static String standardize(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new NumberFormatException("Phone number cannot be null or blank!");
        }

        if (phone.length() == 10 && phone.startsWith("0")) {
            return "84" + phone.substring(1);
        }

        if (phone.length() == 11 && phone.startsWith("84")) {
            return phone;
        }

        throw new NumberFormatException("Phone number '" + phone + "' is in invalid format!");
    }
}
