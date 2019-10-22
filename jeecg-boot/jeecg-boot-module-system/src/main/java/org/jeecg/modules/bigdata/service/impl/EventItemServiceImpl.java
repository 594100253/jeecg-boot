package org.jeecg.modules.bigdata.service.impl;

import org.jeecg.modules.bigdata.entity.EventItem;
import org.jeecg.modules.bigdata.mapper.EventItemMapper;
import org.jeecg.modules.bigdata.service.IEventItemService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 事件维度关系表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
@Service
public class EventItemServiceImpl extends ServiceImpl<EventItemMapper, EventItem> implements IEventItemService {

}
