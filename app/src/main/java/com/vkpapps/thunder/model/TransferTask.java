package com.vkpapps.thunder.model;

import com.vkpapps.thunder.connection.FileService;

/**
 * @author VIJAY-PATIDAR
 */
public class TransferTask {
    private RequestInfo requestInfo;
    private FileService fileService;

    public TransferTask(RequestInfo requestInfo, FileService fileService) {
        this.requestInfo = requestInfo;
        this.fileService = fileService;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public FileService getFileService() {
        return fileService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
}
