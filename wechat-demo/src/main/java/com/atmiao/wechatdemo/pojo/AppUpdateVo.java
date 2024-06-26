package com.atmiao.wechatdemo.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author miao
 * @version 1.0
 */
@Data
public class AppUpdateVo implements Serializable {
    private static final long serialVersionUID = -6858810118958887514L;
    private String version;
    private Long size;
    private List<String> updateList;
    private String fileName;
    private Integer fileType;
    private String outerLink;

}
