package dev.tomasgarcia.ultimatejoinplus.data;

import java.sql.Timestamp;

public class PlayerData {
    private final String uuid;
    private final String name;
    private int joins;
    private Timestamp firstJoin;
    private Timestamp lastJoin;

    public PlayerData(String uuid, String name, int joins, Timestamp firstJoin, Timestamp lastJoin) {
        this.uuid = uuid;
        this.name = name;
        this.joins = joins;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getJoins() {
        return joins;
    }

    public void setJoins(int joins) {
        this.joins = joins;
    }

    public Timestamp getFirstJoin() {
        return firstJoin;
    }

    public Timestamp getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(Timestamp lastJoin) {
        this.lastJoin = lastJoin;
    }

    public void incrementJoins() {
        this.joins++;
    }
}
