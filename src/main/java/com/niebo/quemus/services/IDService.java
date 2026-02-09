package com.niebo.quemus.services;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class IDService {
    private static final SecureRandom RANDOM = new SecureRandom();


    public String generateDeviceID(){
        return UUID.randomUUID().toString();
    }

    public Long generateGameID() {
    long id = 0;
    for (int i = 0; i < 6; i++) {
        id = id * 10 + RANDOM.nextInt(10); 
    }
    return id;
}

}
