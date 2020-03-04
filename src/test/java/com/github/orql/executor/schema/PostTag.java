package com.github.orql.executor.schema;

import com.github.orql.core.annotation.*;

@Schema
public class PostTag {

    @BelongsTo
    private Post post;

    @BelongsTo
    private Tag tag;

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
