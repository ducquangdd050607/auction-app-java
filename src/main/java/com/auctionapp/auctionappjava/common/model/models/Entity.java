package com.auctionapp.auctionappjava.common.model.models;

public abstract class Entity {
    private String id;
    private String name;

    public Entity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
}
