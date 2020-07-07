package com.vkpapps.thunder.model;

public class BaseInfo {
    protected String name;
    protected String source;
    protected String id;
    protected int type;

    public BaseInfo(String name, String source, String id, int type) {
        this.name = name;
        this.source = source;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
