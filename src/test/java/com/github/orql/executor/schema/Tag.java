package com.github.orql.executor.schema;

import com.github.orql.core.annotation.*;

import java.util.List;

@Schema
public class Tag {

    @Column(primaryKey = true, generatedKey = true)
    private Long id;

    @Column
    private String name;

    @BelongsToMany(middle = PostTag.class)
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

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
