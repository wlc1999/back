package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.Menu;
import org.apache.ibatis.annotations.Param;
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
@Repository
public interface AdminMapper extends BaseMapper<Admin> {


    /**
     * 获取所有操作员
     * @param id   @Param("id")  设置xml中使用该传入参数时需要的名字
     * @param keywords
     * @return
     */
    List<Admin> getAllAdmins(@Param("id") Integer id, @Param("keywords") String keywords);

    void newLogin(Admin admin);
}
