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
public interface IKylinService extends IService<EventDic> {


	// 单事件图表数据
	Map<String,Object> querySingleEventChart(Map map);
	// 单事件表格数据
	Map<String,Object> querySingleEventTable(Map map);

}
