package org.jeecg.modules.bigdata.service.impl;

import org.jeecg.modules.bigdata.entity.TargetDic;
import org.jeecg.modules.bigdata.mapper.EventDicMapper;
import org.jeecg.modules.bigdata.mapper.TargetDicMapper;
import org.jeecg.modules.bigdata.service.ITargetDicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * @Description: 指标配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
@Service
public class TargetDicServiceImpl extends ServiceImpl<TargetDicMapper, TargetDic> implements ITargetDicService {


	@Autowired
	private EventDicMapper eventDicMapper;

	@Override
	public List<Map> getEventTargets(Integer eventId) {
		String sql="select td.target_name label,td.target_name_column value,td.if_checked ifChecked  from bu_event_dic ed " +
			"LEFT JOIN bu_event_target et on et.event_id = ed.id " +
			"LEFT JOIN bu_target_dic td on td.id = et.target_id " +
			"where ed.id = '"+eventId+"' " +
			"order by td.sort_no";
		List<Map> list  = eventDicMapper.superSelect(sql);

		return list;
	}

	@Override
	public List<Map> getEventTargetIds(Integer eventId) {
		String sql="select td.target_name label,td.id value,td.if_checked ifChecked  from bu_event_dic ed " +
			"LEFT JOIN bu_event_target et on et.event_id = ed.id " +
			"LEFT JOIN bu_target_dic td on td.id = et.target_id " +
			"where ed.id = '"+eventId+"' " +
			"order by td.sort_no";
		List<Map> list  = eventDicMapper.superSelect(sql);

		return list;
	}
}
