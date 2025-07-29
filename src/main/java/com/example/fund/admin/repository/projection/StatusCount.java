package com.example.fund.admin.repository.projection;

/* JPA projection for status/count */
public interface StatusCount {
    String getStatus();
    Long getCnt();
}