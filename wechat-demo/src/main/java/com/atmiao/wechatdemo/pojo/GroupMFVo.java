package com.atmiao.wechatdemo.pojo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author miao
 * @version 1.0
 */
@Data
public class GroupMFVo {
   //commonUtils自己生成,非必须
   private String groupId;
   @NotEmpty
   private String groupName;
   private String groupNotice;
   @NotNull
   private Integer  joinType;
   //这样就可以避免非必需的null值传参导致异常
   private MultipartFile avatarFile;
   private MultipartFile avatarCover;

}
