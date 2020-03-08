package com.towercraft.utils;

public class ServerModel {
    private String name;
    private String group;
    private int maxPlayers;
    private int nowPlayer;
    private boolean inGame;
    private String map;

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

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }
}
