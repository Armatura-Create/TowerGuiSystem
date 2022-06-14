package me.towercraft.service.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TypeStatusServer {

    IN_GAME("in_game"),
    STARTING("starting"),
    OFFLINE("offline"),
    ONLINE("online");

    @Getter
    private final String nameConfig;
}
