package com.vkpapps.thunder.model;
/***
 * @author VIJAY PATIDAR
 */
public class PhotoInfo {
    private String name,path;

    public PhotoInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
