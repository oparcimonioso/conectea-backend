package com.conectea.backend.dto;

public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private String date;
    private String userName;
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}