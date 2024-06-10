package com.atmiao.wechatdemo.config;

import com.alibaba.druid.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author miao
 * @version 1.0
 */
@Component
public class AppConfig {
    @Value("${ws.port:}")
    private Integer wsPort;
    @Value("${project.folder:}")
    private String projectFolder;
    @Value("${admin.emails:}")
    private String adminEmails;

    public Integer getWsPort() {
        return wsPort;
    }

    public String getProjectFolder() {
        if(StringUtils.isEmpty(projectFolder) && !projectFolder.endsWith("/")){
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }
}
