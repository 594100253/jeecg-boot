package org.jeecg.modules.bigdata.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.bigdata.entity.EventDic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 事件配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-17
 * @Version: V1.0
 */
public interface EventDicMapper extends BaseMapper<EventDic> {

	List<Map> superSelect(@Param("sql") String sql);
}
