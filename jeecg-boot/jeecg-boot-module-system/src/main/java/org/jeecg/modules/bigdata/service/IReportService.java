package org.jeecg.modules.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.bigdata.entity.EventDic;

import java.util.Map;

/**
 * @Description: 事件配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-17
 * @Version: V1.0
 */
public interface IReportService extends IService<EventDic> {

	Map<String,Object> queryReportChart(Map map);
	Map<String,Object> queryReportTable(Map map);

}
