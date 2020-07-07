package com.vkpapps.thunder.model;

/***
 * @author VIJAY PATIDAR
 * */
public class AudioInfo {

    private String path;
    private String name;
    private boolean selected;


    public AudioInfo() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
