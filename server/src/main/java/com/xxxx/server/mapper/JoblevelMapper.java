package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.server.pojo.Joblevel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
public interface JoblevelMapper extends BaseMapper<Joblevel> {

    void deleteCar();

    void incar(@Param("name") String name, @Param("money") String money);

    List<Joblevel> initcar();
}
