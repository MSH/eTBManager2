package org.msh.tb.cases.tags;

import java.util.ArrayList;
import java.util.List;

/**
 * Store a tag report list from a specific tag category. Used in the case home page
 * to display the list of consolidated tags
 * Created by rmemoria on 17/6/15.
 */
public class TagCategory {
    private String name;
    private List<TagItem> tags = new ArrayList<TagItem>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TagItem> getTags() {
        return tags;
    }

    public void setTags(List<TagItem> tags) {
        this.tags = tags;
    }
}
