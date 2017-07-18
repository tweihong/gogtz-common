package com.gogtz.common.bean;

import java.io.Serializable;

/**
 * <strong>键值对用的Bean</strong>
 *
 * @author t
 * @Time 2017年7月18日11:26:31
 */
public class KeyValueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public KeyValueBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
