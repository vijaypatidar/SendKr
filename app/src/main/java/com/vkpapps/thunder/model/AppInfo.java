package com.vkpapps.thunder.model;

import android.graphics.drawable.Drawable;
/***
 * @author VIJAY PATIDAR
 */
public class AppInfo {
    private String name,source;
    private Drawable icon;
    private boolean selected;

    public AppInfo(String name, String source, Drawable icon) {
        this.name = name;
        this.source = source;
        this.icon = icon;
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
