package com.github.orql.executor.schema;

import com.github.orql.executor.annotation.BelongsTo;
import com.github.orql.executor.annotation.BelongsToMany;
import com.github.orql.executor.annotation.Column;
import com.github.orql.executor.annotation.Schema;

import java.util.Date;
import java.util.List;

@Schema
public class Post {

    @Column(primaryKey = true, generatedKey = true)
    private Long id;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private Date createAt;

    @BelongsTo(refKey = "authorId")
    private User author;

    @BelongsToMany(middle = PostTag.class)
    private List<Tag> tags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
