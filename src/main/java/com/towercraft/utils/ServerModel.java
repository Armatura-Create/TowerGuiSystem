package com.towercraft.utils;

public class ServerModel {
    private String name;
    private String group;
    private int maxPlayers;
    private int nowPlayer;
    private String inGame;
    private String map;

    public ServerModel() {}

    public ServerModel(String name, String group, String map, String inGame, int nowPlayer, int maxPlayers) {
        this.name = name;
        this.group = group;
        this.maxPlayers = maxPlayers;
        this.nowPlayer = nowPlayer;
        this.inGame = inGame;
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getNowPlayer() {
        return nowPlayer;
    }

    public void setNowPlayer(int nowPlayer) {
        this.nowPlayer = nowPlayer;
    }

    public String getInStatus() {
        return inGame;
    }

    public void setStatus(String inGame) {
        this.inGame = inGame;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }
}
