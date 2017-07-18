package com.gogtz.common.jstree;

import lombok.Data;

import java.io.Serializable;

@Data
public class JSTreeNodeState implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * // is the node selected
     */
    private boolean selected;

    /**
     * is the node open
     */
    private boolean opened;

    /**
     * is the node disabled
     */
    private boolean disabled;
}
