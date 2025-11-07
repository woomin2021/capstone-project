package com.example.jjb20.chat;

public class ChatMsgVO {

    private String userid;
    private String crt_dt;
    private String content;

    public ChatMsgVO() {
    }

    public ChatMsgVO(String userid, String crt_dt, String content) {
        this.userid = userid;
        this.crt_dt = crt_dt;
        this.content = content;
    }

    public String getUserid() {
        return userid;
    }

    public String getCrt_dt() {
        return crt_dt;
    }

    public String getContent() {
        return content;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setCrt_dt(String crt_dt) {
        this.crt_dt = crt_dt;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChatMsgVO{" +
                "userid='" + userid + '\'' +
                ", crt_dt='" + crt_dt + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
