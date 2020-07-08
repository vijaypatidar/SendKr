package com.vkpapps.thunder.interfaces;

import com.vkpapps.thunder.model.RawRequestInfo;

import java.util.List;
/***
 * @author VIJAY PATIDAR
 */
public interface OnFileRequestPrepareListener {
    void sendFiles(List<RawRequestInfo> requests, int type);
}
