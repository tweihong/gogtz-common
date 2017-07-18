package com.gogtz.common.jstree;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class JSTreeNode implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * JStree的唯一ID "string" // required
     */
    private String id;

    /**
     * JStree的唯一ID "string" // required
     */
    private String tid;


    /**
     * parent "string" // required
     */
    private String parent;

    /**
     * 显示的文本"string" // node text
     */
    private String text;

    /**
     * 显示的图片 "string" // string for custom
     */
    private String icon;

    /**
     * attributes for the generated LI node
     */
    private JSONObject li_attr;

    /**
     * attributes for the generated A node
     */
    private JSONObject a_attr;

    /**
     * 子树
     */
    private List<JSTreeNode> children;


    /**
     * 节点状态
     */
    private JSTreeNodeState state;

    /**
     * rowSpan
     */
    private int rowSpan;

}
