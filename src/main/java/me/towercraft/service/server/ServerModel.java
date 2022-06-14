package me.towercraft.service.server;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServerModel {
    private String name;
    private String group;
    private Integer maxPlayers;
    private Integer nowPlayer;
    private TypeStatusServer status;
    private String mapName;
    private Boolean dynamic;
}
