package org.jeecg.modules.bigdata.service;

import org.jeecg.modules.bigdata.entity.ItemDic;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 维度配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
public interface IItemDicService extends IService<ItemDic> {



	List<Map> getEventItems(Integer eventId);

	List<Map> getEventItemIds(Integer eventId);

	/**
	 * 根据事件名称 查询该事件绑定的 所有层级维度最底层的维度列和
	 * 简单理解: 年 月 日 时 是层级维度，若维度有时，则返回 年-月-日-时
	 *          （字段：db_column_sql  时配的是 timeyear,timemonth,timeday,timehour），
	 * @param map
	 * @return 层级维度ID，层级维度db_column_name,层级展示的所有列
	 */
	List<Map> getConcatHierarchyByEvent(Map map);

	Map getConcatColumn(Map map);

	/**
	 * 查询指定事件的 维度(层级维度和非层级维度)和指标
	 * @param map
	 * @return 维度title 维度name 、sqlname
	 */
	Map getConcatItemsTargetsByEvent(Map map);
}
