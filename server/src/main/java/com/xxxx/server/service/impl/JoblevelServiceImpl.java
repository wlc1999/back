package com.xxxx.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.server.mapper.JoblevelMapper;
import com.xxxx.server.pojo.Joblevel;
import com.xxxx.server.service.IJoblevelService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */

@Service
public class JoblevelServiceImpl extends ServiceImpl<JoblevelMapper, Joblevel> implements IJoblevelService {

    @Autowired
    JoblevelMapper joblevelMapper;
    @Override
    public void deleteCar() {
        joblevelMapper.deleteCar();
    }

    @Override
    public void incar(Joblevel joblevel) {
        joblevel.getName();
        joblevel.getMoney();
        joblevelMapper.incar(joblevel.getName(),joblevel.getMoney());
    }

    @Override
    public List<Joblevel> initcar() {
        return joblevelMapper.initcar();
    }
}
