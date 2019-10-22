package org.jeecg.modules.bigdata.service.impl;

import org.jeecg.modules.bigdata.entity.ItemDic;
import org.jeecg.modules.bigdata.mapper.EventDicMapper;
import org.jeecg.modules.bigdata.mapper.ItemDicMapper;
import org.jeecg.modules.bigdata.service.IItemDicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * @Description: 维度配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
@Service
public class ItemDicServiceImpl extends ServiceImpl<ItemDicMapper, ItemDic> implements IItemDicService {

	@Autowired
	private EventDicMapper eventDicMapper;

	@Override
	public List<Map> getConcatHierarchyByEvent(Map map) {

		// 没有必要 根据事件查询该事件下的维度，map.get("items")已经可以指定了是那些维度
//		String sql="select is_hierarchy hierarchyId,max(id.db_column_sql) hierarchyName\n" +
//			"from bu_event_dic ed\n" +
//			"left join bu_event_item ei on ei.event_id = ed.id\n" +
//			"LEFT JOIN bu_item_dic id on id.id = ei.item_id\n" +
//			"where ed.event_name='"+map.get("eventName")+"' and is_hierarchy !=0\n" +
//			"and db_column_name in ("+map.get("items")+")\n" +
//			"GROUP BY is_hierarchy";

		String sql="select is_hierarchy hierarchyId,db_column_name bottomName,max(id.db_column_sql) hierarchyName\n" +
			"from bu_item_dic id \n" +
			"where is_hierarchy !=0 and db_column_name in ("+map.get("items")+")\n" +
			"GROUP BY is_hierarchy";
		List<Map> list  = eventDicMapper.superSelect(sql);
		return list;
	}

	@Override
	public Map getConcatItemsTargetsByEvent(Map map) {
		String sql="select is_hierarchy\n" +
			"from bu_item_dic id \n" +
			"where is_hierarchy !=0 and db_column_name in ("+map.get("items")+")\n" +
			"GROUP BY is_hierarchy";
		List<Map> list  = eventDicMapper.superSelect(sql);

		StringBuffer sqlHierarchyItemSb = new StringBuffer();
		// 非层级维度
		sqlHierarchyItemSb.append(
			"\t\t\t(\n" +
				"\t\t\t\tselect id.item_min_width width,id.item_name tableTitle,id.db_column_name tableName,id.db_column_sql selectName,id.sort_no,id.id\n" +
				"\t\t\t\tFROM bu_event_dic ed\n" +
				"\t\t\t\tLEFT JOIN bu_event_item ei ON ei.event_id = ed.id\n" +
				"\t\t\t\tLEFT JOIN bu_item_dic id ON id.id = ei.item_id\n" +
				"\t\t\t\tWHERE\n" +
				"\t\t\t\t\ted.event_name = '"+map.get("eventName")+"'\n" +
				"\t\t\t\tAND is_hierarchy = 0\n" +
				"\t\t\t\tAND db_column_name IN ("+map.get("items")+")\n" +
				"\t\t\t)\n"
		);
		// 层级维度 sql
		for (int i = 0; i < list.size() ; i++) {
			sqlHierarchyItemSb.append(
				"\t\t\tunion\n" +
					"\t\t\t( \n" +
					"\t\t\t\tselect item_min_width width,item_name tableTitle,db_column_name tableName,db_column_sql selectName,sort_no,id\n" +
					"\t\t\t\tfrom bu_item_dic where is_hierarchy ='"+list.get(i).get("is_hierarchy")+"'\n" +
					"\t\t\t\tand db_column_name in ("+map.get("items")+")\n" +
					"\t\t\t\torder by id desc LIMIT 1\n" +
					"\t\t\t)\n"
			);
		}
		sqlHierarchyItemSb.insert(0,
			"\t\tselect width,tableTitle,tableName,selectName\n" +
				"\t\tfrom(\n"
		);
		sqlHierarchyItemSb.append("\t\t)  i order by sort_no,id\n");
		sqlHierarchyItemSb.insert(0,
			"\tselect\n" +
				"\t\tGROUP_CONCAT(item.width) width,\n" +
				"\t\tGROUP_CONCAT(item.tableTitle) tableTitle,GROUP_CONCAT(upper(item.tableName)) tableName,\n" +
				"\t\tGROUP_CONCAT(item.selectName) selectName,GROUP_CONCAT(item.selectName) groupName,null fromName\n" +
				"\tfrom (\n");
		sqlHierarchyItemSb.append("\t) item\n");

		// 拼接 指标 sql
		sqlHierarchyItemSb.insert(0,
			"select " +
				"\tGROUP_CONCAT(a.width) width,\n" +
				"\tGROUP_CONCAT(a.tableTitle) tableTitle,GROUP_CONCAT(upper(a.tableName)) tableName,\n" +
				"\tGROUP_CONCAT(a.selectName) selectName,GROUP_CONCAT(a.groupName) groupName,\n" +
				"\tGROUP_CONCAT(a.fromName) fromName\n" +
				"from (\n"
		);
		sqlHierarchyItemSb.append(
			"\tunion all\n" +
				"\t(\n" +
				"\t\tselect\n" +
				"\t\t\tGROUP_CONCAT(t.width) width,\n" +
				"\t\t\tGROUP_CONCAT(t.target_name) tableTitle,GROUP_CONCAT(t.target_alias) tableName,\n" +
				"\t\t\tGROUP_CONCAT(CONCAT(t.target_name_function,' as ',t.target_alias)) selectName,\n" +
				"\t\t\tnull groupName,t.event_table fromName\n" +
				"\t\tFROM (\n" +
				"\t\t\tselect td.target_min_width width,td.target_name,td.target_alias,td.target_name_function,ed.event_table\n" +
				"\t\t\tfrom bu_event_dic ed\n" +
				"\t\t\tleft join bu_event_target et on ed.id = et.event_id\n" +
				"\t\t\tLEFT JOIN bu_target_dic td on td.id = et.target_id\n" +
				"\t\t\twhere ed.event_name='"+map.get("eventName")+"'\n" +
				"\t\t\tand target_alias in ("+map.get("targets")+")\n" +
				"\t\t\tORDER BY td.sort_no) t\n" +
				"\t)\n"
		);

		sqlHierarchyItemSb.append(
			") a\n"
		);

		List<Map> listHierarchyItem  = eventDicMapper.superSelect(sqlHierarchyItemSb.toString());
		return listHierarchyItem.get(0);
	}

}
