package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.xxxx.server.pojo.Role;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
@Repository  //标签防止爆红
public interface RoleMapper extends BaseMapper<Role> {
    /**
     * 根据用户id查询角色列表
     * @param adminId
     * @return
     */
    List<Role> getRole(Integer adminId);
}
