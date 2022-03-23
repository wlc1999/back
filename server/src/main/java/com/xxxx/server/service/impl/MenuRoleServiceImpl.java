package com.xxxx.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xxxx.server.mapper.MenuRoleMapper;
import com.xxxx.server.pojo.MenuRole;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.service.IMenuRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
@Service
public class MenuRoleServiceImpl extends ServiceImpl<MenuRoleMapper, MenuRole> implements IMenuRoleService {

    @Autowired
    private MenuRoleMapper menuRoleMapper;
    /**
     * 更新角色菜单
     * @param rid
     * @param mids
     * @return
     */
    @Override
    @Transactional
    public RespBean updateMenuRole(Integer rid, Integer[] mids) {
        menuRoleMapper.delete(new QueryWrapper<MenuRole>().eq("rid",rid));  //删除了这个角色下面所有的菜单
        if (null==mids||0==mids.length){ //只需要删除菜单时，mids传来的是空的
            return RespBean.success("更新成功mids=0");
        }
        /**
         * 批量更新
         */
        Integer integer=menuRoleMapper.insertRecord(rid,mids);
        if (mids.length==integer){
            return RespBean.success("更新成功mids!=0");
        }
        return RespBean.error("更新失败");
    }
}
