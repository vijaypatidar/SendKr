package com.vkpapps.thunder.model;

import java.io.Serializable;

/***
 * @author VIJAY PATIDAR
 * */
public class FileRequest implements Serializable {

    public static final int DOWNLOAD_REQUEST = 13;
    public static final int UPLOAD_REQUEST_CONFIRM = 14;
    public static final int DOWNLOAD_REQUEST_CONFIRM = 15;

    private int action;
    private String fileName;
    private String id;
    private int type;

    public FileRequest(int action, String fileName, String id, int type) {
        this.action = action;
        this.fileName = fileName;
        this.id = id;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getData() {
        return fileName;
    }

    public void setData(String fileName) {
        this.fileName = fileName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
