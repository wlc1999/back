package com.xxxx.server.controller;

import com.xxxx.server.Utils.FastDFSUtils;
import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.service.IAdminService;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class AdminInfoController {
    @Autowired
    private IAdminService adminService;

    @ApiOperation(value = "更新当前用户信息")
    @PutMapping("/admin/info")
    public RespBean updateAdmin(@RequestBody Admin admin, Authentication authentication/*为了将更新后的authentication重新注入到上下文中*/){
        if(adminService.updateById(admin)){
            //将更新后的数据Authentication再次放入
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(admin,null,authentication.getAuthorities()));
            return RespBean.success("更新成功");
        }
        return RespBean.error("更新失败！");
    }

    @ApiOperation(value = "更新用户密码")
    @PutMapping("/admin/pass")
    public RespBean updateAdminPassword(@RequestBody Map<String,Object>/*为了获取老密码和新密码*/ info){
        String oldPass = (String) info.get("oldPass");//获取老密码
        String pass = (String) info.get("pass");//获取新密码
        Integer adminId = (Integer) info.get("adminId");//对应用户的id
        return adminService.updateeeePassword(oldPass,pass,adminId);
    }
    @ApiOperation(value = "更新用户头像")
    @PostMapping("/admin/userface")
    public RespBean updateHrUserFace(MultipartFile file, Integer id/*用户id*/, Authentication authentication) {
//获取上传文件地址
        String[] fileAbsolutePath = FastDFSUtils.upload(file);//获取到一个数组
        //准备url
        String url = FastDFSUtils.getTrackerUrl() + fileAbsolutePath[0] + "/" + fileAbsolutePath[1];
        return adminService.updateAdminUserFace(url/*将拿到的url传入*/, id, authentication);
    }

    }
