package org.jeecg.modules.bigdata.service;

import org.jeecg.modules.bigdata.entity.TargetDic;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 指标配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
public interface ITargetDicService extends IService<TargetDic> {

	List<Map> getEventTargets(Integer eventId);

	List<Map> getEventTargetIds(Integer eventId);
}
