package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.request.OtpRequest;
import com.viettel.vtag.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class OtpServiceImpl implements OtpService {

    @Value("${vtag.otp.length}")
    private int length;



    private SecureRandom secureRandom;

    {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String generate(String allowedChars) {
        var characters = allowedChars.toCharArray();
        var randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        var chars = new char[length];
        for (int i = 0, l = randomBytes.length; i < l; i++) {
            chars[i] = characters[(((int) randomBytes[i]) & 0xFF) % characters.length];
        }
        return new String(chars);
    }

    @Override
    public void sendOtp(OtpRequest request, String otp) {

    }
}
