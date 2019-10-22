package org.jeecg.modules.bigdata.service.impl;

import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.bigdata.entity.EventDic;
import org.jeecg.modules.bigdata.entity.TargetDic;
import org.jeecg.modules.bigdata.mapper.EventDicMapper;
import org.jeecg.modules.bigdata.service.IEventDicService;
import org.jeecg.modules.bigdata.service.IKylinService;
import org.jeecg.modules.bigdata.service.ITargetDicService;
import org.jeecg.modules.bigdata.util.KylinUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 事件配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-17
 * @Version: V1.0
 */
@Service
public class KylinServiceImpl extends ServiceImpl<EventDicMapper, EventDic> implements IKylinService {

	@Autowired
	private EventDicMapper eventDicMapper;

	@Autowired
	private IEventDicService eventDicService;
	@Autowired
	private ITargetDicService targetDicService;
	@Autowired
	private ItemDicServiceImpl itemDicService;

	// 单事件 图表
	public Map<String,Object> querySingleEventChart(Map map) {

		Map returnMap = new HashMap();
		// 获取事件
		StringBuffer selectSql = new StringBuffer();
		Integer eventId = Integer.parseInt(map.get("eventId").toString());
		EventDic eventDic= eventDicService.getById(eventId);

		// 日期条件
		StringBuffer whereSb = new StringBuffer();
		String[] dt = map.get("dt").toString().split("~");
		whereSb.append(" AND DT BETWEEN '"+dt[0].trim()+"' and '"+dt[1].trim()+"' ");

		// X轴
		String xAxisName = (String) map.get("xAxisName");
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		String sqlxAxis = "select distinct "+xAxisName+" from "+eventDic.getEventTable()+" where 1=1 "+whereSb +" order by " +xAxisName;
		List<Map<String, Object>> xAxisData = jdbcTemplate.queryForList(sqlxAxis);
		if(xAxisData.size()==0){ // 无结果
			return new HashMap();
		}
		List<String> xAxis = new ArrayList<>();
		for (int i = 0; i < xAxisData.size() ; i++) {
			StringBuffer xAxisSb = new StringBuffer();
			xAxisData.get(i).forEach((key, value) ->{
				if(key.equals("TIMEHOUR")){
					xAxisSb.append(
						" "+KylinUtil.zeroFill(key,value)+":00"
					);
				}else{
					xAxisSb.append(xAxisSb.length()>0 ? "-" : "");
					xAxisSb.append(
						KylinUtil.zeroFill(key,value)
					);
				}
			});
			xAxis.add(xAxisSb.toString());
		}
		returnMap.put("xAxis",xAxis);

		// 2. legend and series
		List<String> targetName = null;
		if(map.get("targetName") instanceof List){
			targetName = (List) map.get("targetName");
		}else if (map.get("targetName") instanceof String){
			targetName = new ArrayList();
			targetName.add(map.get("targetName").toString());
		}
		List<String> legendAr = new ArrayList<>();
		StringBuffer queryDataSql = new StringBuffer();
		List<Map> seriesAr = new ArrayList<>();
		queryDataSql.append("select * from  (\n");
		for (int i = 0; i < targetName.size() ; i++) {


			TargetDic targetDic = targetDicService.lambdaQuery().eq(TargetDic::getTargetNameColumn,targetName.get(i)).one();

			if(i>0){
				queryDataSql.append("\n union \n");
			}
			queryDataSql.append("(\n");
			queryDataSql.append("select "+xAxisName+" xAxisName,"+
				"'"+targetDic.getTargetName()+ "' targetType,"+
				targetDic.getTargetNameFunction()+
				" target from "+eventDic.getEventTable()+" where 1=1 " + whereSb+
				" group by "+xAxisName +" order by " +xAxisName);
			queryDataSql.append("\n)");
		}

		queryDataSql.append("\n)");
		List<Map<String, Object>> queryList =jdbcTemplate.queryForList(queryDataSql.toString());

		for (int i = 0; i < queryList.size() ; i++) {
			queryList.get(i).put(
				"DATE",
				queryList.get(i).get("TIMEYEAR")+"-"+queryList.get(i).get("TIMEMONTH")+"-"+queryList.get(i).get("XAXISNAME")
			);
		}
		returnMap.put("data",queryList);
		return returnMap;
	}

	// 单事件 表格
	public Map<String, Object> querySingleEventTable(Map map) {

		Map returnMap = new HashMap();

		// 获取事件
		StringBuffer selectSql = new StringBuffer();
		Integer eventId = Integer.parseInt(map.get("eventId").toString());
		EventDic eventDic= eventDicService.getById(eventId);

		List<String> itemTableNameAr = null;
		if(map.get("itemName") instanceof List){
			itemTableNameAr = (List) map.get("itemName");
		}else if (map.get("itemName") instanceof String){
			itemTableNameAr = new ArrayList();
			itemTableNameAr.add(map.get("itemName").toString());
		}
		List<String> targetTableNameAr = null;
		if(map.get("targetName") instanceof List){
			targetTableNameAr = (List) map.get("targetName");
		}else if (map.get("targetName") instanceof String){
			targetTableNameAr = new ArrayList();
			targetTableNameAr.add(map.get("targetName").toString());
		}

		// 日期条件
		StringBuffer whereSb = new StringBuffer();
		String[] dt = map.get("dt").toString().split(" ~ ");
		whereSb.append(" AND DT BETWEEN '"+dt[0].trim()+"' and '"+dt[1].trim()+"' ");

		// 根据 事件、维度和指标的条件，查询对应的 title（用于显示列名）、name(列key)、sqlname(sql的select列和groupby的列)
		Map getHierarchyItemMap = new HashMap();
		getHierarchyItemMap.put("eventName",eventDic.getEventName());
		getHierarchyItemMap.put("items","'"+ StringUtils.join(itemTableNameAr,"','") +"'");
		getHierarchyItemMap.put("targets","'"+ StringUtils.join(targetTableNameAr,"','")+ "'");
		Map hierarchyItemMap =itemDicService.getConcatItemsTargetsByEvent(getHierarchyItemMap);

		// table 的 title、name
		String[] tableTitleAr = hierarchyItemMap.get("tableTitle").toString().split(",");
		String[] tableNameAr = hierarchyItemMap.get("tableName").toString().split(",");
		String[] tableNameWidthAr = hierarchyItemMap.get("width").toString().split(",");
		returnMap.put("tableTitle",tableTitleAr);
		returnMap.put("tableName",tableNameAr);
		returnMap.put("tableNameWidth",tableNameWidthAr);

		// 拼接 kylin Sql 查询数据
		String selectName = hierarchyItemMap.get("selectName").toString();
		String groupName = hierarchyItemMap.get("groupName").toString();
		String fromName = hierarchyItemMap.get("fromName").toString();
		StringBuffer queryDataSql = new StringBuffer();
		queryDataSql.append(" select ");
		queryDataSql.append(selectName);
		queryDataSql.append(" from "+fromName);
		queryDataSql.append(" where 1 = 1 " + whereSb);

		// 高级搜索 条件
		if(map.containsKey("searchData")){
			Map<String,Object> searchMap = (Map) map.get("searchData");
			for (Map.Entry<String,Object> entry : searchMap.entrySet()){
				List entryValueToList = (List) entry.getValue();
				queryDataSql.append(" and "+entry.getKey() +" in ("+StringUtils.join(entryValueToList.toArray(),",")+")");
			}
		}
		if(map.containsKey("siteid")){
			queryDataSql.append(" and siteid in( "+map.get("siteid")+")");
		}
		if(map.containsKey("regchannelid")){
			queryDataSql.append(" and regchannelid in( "+map.get("regchannelid")+")");
		}
		if(map.containsKey("lgnchannelid")){
			queryDataSql.append(" and lgnchannelid in( "+map.get("lgnchannelid")+")");
		}
		if(map.containsKey("roomid")){
			queryDataSql.append(" and roomid in( "+map.get("roomid")+")");
		}
		queryDataSql.append(" group by "+groupName);
		queryDataSql.append(" order by " + groupName);

		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		List<Map<String, Object>> queryList =jdbcTemplate.queryForList(queryDataSql.toString());
		returnMap.put("records",queryList);

		// 查询 需要格式化的指标
		List<TargetDic> fmtTargets = targetDicService.lambdaQuery()
			.select(TargetDic::getTargetAlias,TargetDic::getIfFormat)
			.isNotNull(TargetDic::getIfFormat)
			.ne(TargetDic::getIfFormat,"")
			.in(targetTableNameAr.size()>0,TargetDic::getTargetAlias,targetTableNameAr.toArray()).list();

		// 层级维度特殊处理， 若有层级维度，最终只需展示本层级最底层的维度列db_column_sql
		// 查询最底层的db_column_sql 即本次需要展示的列和逗号隔开，再拼接列，最终赋值给最底层维度上。（因为kelin 不支持 concat 只能再结果集特殊处理）
		List<Map> listConcatHierarchy =itemDicService.getConcatHierarchyByEvent(getHierarchyItemMap);
		for (int i = 0; i < queryList.size(); i++) {
			// 层级格式化
			for (int j = 0; j < listConcatHierarchy.size(); j++) {
				StringBuffer sb = new StringBuffer();
				String[] hierarchyName=listConcatHierarchy.get(j).get("hierarchyName").toString().toUpperCase().split(",");
				for (int k = 0; k < hierarchyName.length ; k++) {
					if(hierarchyName[k].equals("TIMEHOUR")){
						sb.append(
							" "+KylinUtil.zeroFill(
								hierarchyName[k],
								queryList.get(i).get(hierarchyName[k])
							)+":00"
						);
					}else{
						sb.append(sb.length() > 0 ? "-":"");
						// 补零
						sb.append(
							KylinUtil.zeroFill(
								hierarchyName[k],
								queryList.get(i).get(hierarchyName[k])
							)
						);
					}
				}
				queryList.get(i).put(hierarchyName[hierarchyName.length-1],sb.toString());
			}

			// 指标格式化
			for (int j = 0; j < fmtTargets.size() ; j++) {
//				if( queryList.get(i).get( fmtTargets.get(j).getTargetAlias().toUpperCase() ) != null
//					&& queryList.get(i-1).get( fmtTargets.get(j).getTargetAlias().toUpperCase() )!= null
//				){
//					queryList.get(i).put(
//						fmtTargets.get(j).getTargetAlias().toUpperCase(),
//
//						(Double.parseDouble(queryList.get(i).get( fmtTargets.get(j).getTargetAlias().toUpperCase() ).toString() )
//						- Double.parseDouble( queryList.get(i-1).get( fmtTargets.get(j).getTargetAlias().toUpperCase() ).toString() )
//						) / Double.parseDouble( queryList.get(i-1).get( fmtTargets.get(j).getTargetAlias().toUpperCase() ).toString() )
//						);
//				}
			}
		}


		return returnMap;
	}


	public JdbcTemplate getJdbcTemplate(){
		DriverManagerDataSource dataSource=new DriverManagerDataSource();
		dataSource.setDriverClassName("org.apache.kylin.jdbc.Driver");
		dataSource.setUrl("jdbc:kylin://47.111.189.12:7070/datatalk");
		dataSource.setUsername("ADMIN");
		dataSource.setPassword("KYLIN");
		return new JdbcTemplate(dataSource);
	}
}
