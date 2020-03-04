package com.github.orql.executor.schema;

import com.github.orql.core.annotation.*;

@Schema
public class UserInfo {

    @Column(primaryKey = true, generatedKey = true)
    private Long id;

    @Column
    private String no;

    @BelongsTo
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
