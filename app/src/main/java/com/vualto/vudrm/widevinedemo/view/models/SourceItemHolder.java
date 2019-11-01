package com.vualto.vudrm.widevinedemo.view.models;

import java.util.List;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

public class SourceItemHolder {

    public String title;
    public List<com.vualto.vudrm.widevinedemo.view.models.SourceItem> sourceItemList;

    public SourceItemHolder(String title, List<com.vualto.vudrm.widevinedemo.view.models.SourceItem> sourceItemList) {
        this.title = title;
        this.sourceItemList = sourceItemList;
    }

    @Override
    public String toString() {
        return "SourceItemHolder{" +
                "title='" + title + '\'' +
                ", sourceItemList=" + sourceItemList +
                '}';
    }

}
