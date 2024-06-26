package com.atmiao.wechatdemo.pojo;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName app_update
 */
@TableName(value ="app_update")
@Data
public class AppUpdate implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "version")
    private String version;

    /**
     * 
     */
    @TableField(value = "update_desc")
    private String updateDesc;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 0,未发布1 灰度发布 2 全网发布
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "grayscale_uid")
    private String grayscaleUid;

    /**
     * 文件类型 0：本地文件 1 外链地址
     */
    @TableField(value = "file_type")
    private Integer fileType;

    /**
     * 
     */
    @TableField(value = "outer_link")
    private String outerLink;
    @TableField(exist = false)
    private String[] updateDescArray;
    //这个反射肯定会调用
    public String[] getUpdateDescArray() {
        if(!StringUtils.isEmpty(updateDesc)){
            updateDescArray = this.updateDesc.split("\\|");
        }
        return updateDescArray;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}