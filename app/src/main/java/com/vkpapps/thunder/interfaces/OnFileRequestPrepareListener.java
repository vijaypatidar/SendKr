package com.vkpapps.thunder.interfaces;

import com.vkpapps.thunder.model.FileRequest;

import java.util.List;
/***
 * @author VIJAY PATIDAR
 */
public interface OnFileRequestPrepareListener {
    void sendFiles(List<FileRequest> requests,int type);
}
