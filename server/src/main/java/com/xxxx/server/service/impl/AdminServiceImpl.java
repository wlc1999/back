package com.xxxx.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xxxx.server.AdminUtils;
import com.xxxx.server.config.security.component.JwtTokenUtils;
import com.xxxx.server.mapper.AdminMapper;
import com.xxxx.server.mapper.AdminRoleMapper;
import com.xxxx.server.mapper.RoleMapper;
import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.AdminRole;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.pojo.Role;
import com.xxxx.server.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private UserDetailsService userDetailsService;  //是通过这个获取用户名和密码的
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtils jwtTokenUtils;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private AdminRoleMapper adminRoleMapper;

    @Autowired
    private HttpSession session;

    /**
     * 登陆之后返回token
     * @param username
     * @param password
     * @param code
     * @return
     */
    @Override
    public RespBean login(String username, String password, String code, HttpServletRequest request) {
        String captcha=(String) request.getSession().getAttribute("captcha");  //获取到验证码，转化为String类型
        if (StringUtils.isEmpty(code)||!captcha.equalsIgnoreCase(code)){  /*captcha是获取的存在于Session中的，code是用户输入的*/
            return RespBean.error("验证码输入错误，请重新输入");
        }

        //登录
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);//这是在后台获取用户名和密码
        if (null == userDetails){
            return RespBean.error("用户记录不存在");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {//对比前端传的密码和userDetails获取到的密码
            return RespBean.error("用户名或密码不正确");

        }
        if (!userDetails.isEnabled()){
            return RespBean.error("账号被禁用，请联系管理员");
        }

        //更新security登录用户对象
        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userDetails/*SpringSc提供的里面包含用户信息*/,null/*凭证，也就是密码一般不返回*/,userDetails.getAuthorities()/*权限列表*/);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);


        String token = jwtTokenUtils.generateToken(userDetails);//如果没有错误就拿到我们需要的token
        Map<String,String> tokenMap=new HashMap<>();
        tokenMap.put("token",token);
        tokenMap.put("tokenHead",tokenHead);// tokenHead是给前端让他放在请求头里面的
        return RespBean.success("登陆成功",tokenMap);
    }

    @Override
    public Admin getAdminByUserName(String username) {
        return adminMapper.selectOne(new QueryWrapper<Admin>().eq("username"/*表中的这个字段*/,username).eq("enabled",true)/*判断用户是否被禁用*/);
    }

    /**
     * 根据用户id查询角色列表
     * @param adminId
     * @return
     */
    @Override
    public List<Role> getRole(Integer adminId) {
        return roleMapper.getRole(adminId);
    }

    /**
     * 获取所有操作员
     * @param keywords
     * @return
     */
    @Override
    public List<Admin> getAllAdmins(String keywords) {
        return adminMapper.getAllAdmins(AdminUtils.getCurrentAdmin().getId()/*当前登录的用户id*/,keywords);
    }

    /**
     * 更新操作员角色
     * @param adminId
     * @param rids
     * @return
     */
    @Override
    public RespBean updateAdminRole(Integer adminId, Integer[] rids) {
        adminRoleMapper.delete(new QueryWrapper<AdminRole>().eq("adminId",adminId));//首先全部删除
        Integer integer= adminRoleMapper.updateAdminRole(adminId,rids);
        if (rids.length==integer){
            return RespBean.success("更新成功");
        }
        return RespBean.error("更新失败");
    }

    /**
     * 更新用户密码
     * @param adminId
     * @return
     */
    @Override
    public RespBean updateeeePassword(String oldPass, String pass, Integer adminId) {
       Admin admin=adminMapper.selectById(adminId);//拿到admin对象，为了和旧密码进行比较
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();//处理加密密码
        if (encoder.matches(oldPass,admin.getPassword())){//判断新老密码是否一致
            admin.setPassword(encoder.encode(pass)/*新密码也要encode进行加密*/);
            int result=adminMapper.updateById(admin);
            if (1==result){
                return RespBean.success("密码更新成功");
            }

        }
        return RespBean.error("密码更新失败");
    }

    /**
     * 更新用户头像
     * @param url
     * @param id
     * @param authentication
     * @return
     */
    @Override
    public RespBean updateAdminUserFace(String url, Integer id, Authentication authentication) {
        Admin admin = adminMapper.selectById(id);//拿到admin对象
        admin.setUserFace(url);//更新url  到这头像就应该更新好了
        int result = adminMapper.updateById(admin);
        if (1==result){//判断是不是更新了一条，是的话就更新全局变量
            Admin principal = (Admin) authentication.getPrincipal();
            principal.setUserFace(url);//更新url
//更新Authentication
            SecurityContextHolder.getContext().setAuthentication(new
                    UsernamePasswordAuthenticationToken(admin,
                    authentication.getCredentials(),authentication.getAuthorities()));
            return RespBean.success("更新成功!",url);
        }
        return RespBean.error("更新失败!");
    }

    @Override
    public RespBean newLogin(Admin admin) {
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();//处理加密密码
        admin.setPassword(encoder.encode(admin.getPassword()));
        adminMapper.newLogin(admin);
        return RespBean.success("注册成功，更多权限请联系管理员");
    }


}
