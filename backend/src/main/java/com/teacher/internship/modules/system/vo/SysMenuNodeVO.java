package com.teacher.internship.modules.system.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysMenuNodeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String menuType;
    private String routePath;
    private String componentPath;
    private String permissionCode;
    private String icon;
    private Integer sortNo;
    private Integer visible;
    private String status;
    private boolean selected;
    private List<SysMenuNodeVO> children = new ArrayList<>();
}

