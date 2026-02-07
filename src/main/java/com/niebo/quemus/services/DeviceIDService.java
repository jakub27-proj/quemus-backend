package com.niebo.quemus.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class DeviceIDService {
    public String generateDeviceID(){
        return UUID.randomUUID().toString();
    }
}
