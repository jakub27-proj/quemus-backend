package com.niebo.quemus.controllers;

import com.niebo.quemus.models.game.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game/{game_ID}")
    public void sendMessage(Notification notification, @DestinationVariable long game_ID){
        String destination = "/topic/game/" + game_ID;
        messagingTemplate.convertAndSend(destination, notification);
    }
}
