package com.xxxx.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.server.pojo.Joblevel;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
public interface IJoblevelService extends IService<Joblevel> {

    void deleteCar();

    void incar(Joblevel joblevel);

    List<Joblevel> initcar();
}
