package org.msh.tb.cases.tags;

import org.msh.tb.entities.Tag;

/**
 * Store information about a tag to be displayed in the tag report
 * Created by rmemoria on 17/6/15.
 */
public class TagItem {
    private String name;
    private Long total;
    private Tag.TagType type;
    private Integer tagId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Tag.TagType getType() {
        return type;
    }

    public void setType(Tag.TagType type) {
        this.type = type;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }
}
