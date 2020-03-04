package com.github.orql.executor.schema;

import com.github.orql.core.annotation.*;

import java.util.List;

@Schema
public class User {

    @Column(primaryKey = true, generatedKey = true)
    private Long id;

    @Column
    private String name;

    @Column
    private String password;

    @HasOne
    private UserInfo info;

    @HasMany(refKey = "authorId")
    private List<Post> posts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserInfo getInfo() {
        return info;
    }

    public void setInfo(UserInfo info) {
        this.info = info;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
