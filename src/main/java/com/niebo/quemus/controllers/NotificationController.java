package com.niebo.quemus.controllers;

import com.niebo.quemus.models.game.Notification;
import com.niebo.quemus.models.game.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class NotificationController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<Long, String> gameLocks = new ConcurrentHashMap<>();

    @MessageMapping("/game/{game_ID}")
    public void sendMessage(
            Notification notification,
            @DestinationVariable long game_ID) {

        if (notification.getType() == NotificationType.LOCKED_BY_PLAYER) {
            log.info("GAME: " + game_ID + ". Sending locked by player: " + notification.getMessage());
            String existingLock = gameLocks.putIfAbsent(game_ID, notification.getMessage());
            if (existingLock != null) {
                return;
            }
        }
        else if (notification.getType() == NotificationType.UNLOCKED_BY_PLAYER) {
            boolean removed = gameLocks.remove(game_ID, notification.getMessage());
            if (!removed) {
                return;
            }
        }

        String destination = "/topic/game/" + game_ID;
        messagingTemplate.convertAndSend(destination, notification);
    }
}
