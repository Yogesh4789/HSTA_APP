package com.helpdesk.bean;

import java.util.Date;

public class CommentBean {
    private int commentId;
    private int ticketId;
    private int commentedBy;
    private String commentText;
    private Date commentedAt;
    private String commentedByName;

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getCommentedBy() {
        return commentedBy;
    }

    public void setCommentedBy(int commentedBy) {
        this.commentedBy = commentedBy;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Date getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(Date commentedAt) {
        this.commentedAt = commentedAt;
    }

    public String getCommentedByName() {
        return commentedByName;
    }

    public void setCommentedByName(String commentedByName) {
        this.commentedByName = commentedByName;
    }
}
