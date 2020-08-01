package com.vkpapps.thunder.model;

/**
 * @author VIJAY-PATIDAR
 */
public class SerializedMessage {
    private int messageType;
    private String messageBody;

    public SerializedMessage(int messageType, String messageBody) {
        this.messageType = messageType;
        this.messageBody = messageBody;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
}
