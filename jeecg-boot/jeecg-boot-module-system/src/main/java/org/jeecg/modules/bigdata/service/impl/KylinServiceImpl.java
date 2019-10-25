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
import org.jeecg.modules.bigdata.util.DateUtils;
import org.jeecg.modules.bigdata.util.KylinUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.util.*;

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
			queryDataSql.append("select "+xAxisName+","+
				"'"+targetDic.getTargetName()+ "' targetType,"+
				targetDic.getTargetNameFunction()+
				" target from "+eventDic.getEventTable()+" where 1=1 " + whereSb+
				" group by "+xAxisName +" " );
			queryDataSql.append("\n)");
		}

		queryDataSql.append("\n) t order by "+xAxisName);
		List<Map<String, Object>> queryList =jdbcTemplate.queryForList(queryDataSql.toString());

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < queryList.size() ; i++) {
			if(xAxisName.toUpperCase().indexOf("TIMEYEAR")>-1){
				sb.append(queryList.get(i).get("TIMEYEAR"));
			}
			if(xAxisName.toUpperCase().indexOf("TIMEMONTH")>-1){
				sb.append("-"+queryList.get(i).get("TIMEMONTH"));
			}
			if(xAxisName.toUpperCase().indexOf("TIMEDAY")>-1){
				sb.append("-"+queryList.get(i).get("TIMEDAY"));
			}
			if(xAxisName.toUpperCase().indexOf("TIMEHOUR")>-1){
				sb.append(" "+queryList.get(i).get("TIMEHOUR")+":00");
			}
			queryList.get(i).put(
				"DATE",sb.toString()
			);
			sb.setLength(0);
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
		String[] dt = map.get("dt").toString().split("~");
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

	// 留存
	public Map<String,Object> getRetainChart(Map map){

		// 日期条件
		StringBuffer whereSb = new StringBuffer();
		String[] dt = map.get("dt").toString().split("~");
		whereSb.append(" AND DT BETWEEN '"+dt[0].trim()+"' and '"+dt[1].trim()+"' ");

		// 扩展列
		Map<String,String> extendSelectItemMap = new HashMap();
		extendSelectItemMap.put("regsiteid","concat(cast(regsiteid as string),' - ',regsitename) regsiteid");
		extendSelectItemMap.put("regchannelid","concat(cast(regchannelid as string),' - ',regchannelname) regchannelid");
		Map<String,String> extendGroupItemMap = new HashMap();
		extendGroupItemMap.put("regsiteid","regsiteid,regsitename");
		extendGroupItemMap.put("regchannelid","regchannelid,regchannelname");

		String groupitem = map.get("item").toString();
		String selectitem = map.get("item").toString();
		for (Map.Entry<String, String> entry : extendSelectItemMap.entrySet()) {
			selectitem = selectitem.replace(entry.getKey(),entry.getValue());
			groupitem = groupitem.replace(entry.getKey(),extendGroupItemMap.get(entry.getKey()));
		}

		String target = map.get("target").toString();
		String[] targetDic = new String[]{"1day","3day","7day","14day","30day"};
		String[] selectColAr = new String[]{
			"usernum,s1",
			"usernum,s1,s2,s3",
			"usernum,s1,s2,s3,s4,s5,s6,s7",
			"usernum,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14",
			"usernum,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15,s16,s17,s18,s19,s20,s21,s22,s23,s24,s25,s26,s27,s28,s29,s30"};
		List<String> targetList = Arrays.asList(targetDic);
		StringBuffer sql = new StringBuffer();
		// 维度
		sql.append(
			"select " +selectitem+","
		);

		// 移除无效日期 的留存列 比如看最近7天，选的30日留存 实际没有那么多，最多到第七天。
		String newColNames =selectColAr[targetList.indexOf(target)];
		int validDayNum = DateUtils.compareDayNum(DateUtils.StrToDate(dt[0],DateUtils.dayFmt),new Date());
		int retainColCount = newColNames.split(",").length;
		if(retainColCount > validDayNum){
			for (int i = 0; i < retainColCount - validDayNum ; i++) {
				newColNames=newColNames.substring(0,newColNames.lastIndexOf(","));
			}
		}
		// 指标
		sql.append(
			newColNames + "\n"
		);
		sql.append("from (");
		// 内层 相当于table表名
		sql.append(
			"\tselect "+groupitem+",sum(usernum) usernum,\n" +
				"\t\tsum(s1) as s1,sum(s2) as s2,sum(s3) as s3,sum(s4) as s4,sum(s5) as s5,sum(s6) as s6,sum(s7) as s7,\n" +
				"\t\tsum(s8) as s8,sum(s9) as s9,sum(s10) as s10,sum(s11) as s11,sum(s12) as s12,sum(s13) as s13,sum(s14) as s14,\n" +
				"\t\tsum(s15) as s15,sum(s16) as s16,sum(s17) as s17,sum(s18) as s18,sum(s19) as s19,sum(s20) as s20,sum(s21) as s21,\n" +
				"\t\tsum(s22) as s22,sum(s23) as s23,sum(s24) as s24,sum(s25) as s25,sum(s26) as s26,sum(s27) as s27,sum(s28) as s28,sum(s29) as s29,sum(s30) as s30\n" +
				"\tfrom(\n" +
				"\t\tselect actdt,regsiteid,regsitename,regchannelid,regchannelname,viplevel,usernum\n" +
				"\t\t\t,sum(add1dayusernum) as s1,sum(add2daysusernum) as s2,sum(add3daysusernum) as s3,sum(add4daysusernum) as s4,sum(add5daysusernum) as s5\n" +
				"\t\t\t,sum(add6daysusernum) as s6,sum(add7daysusernum) as s7,sum(add8daysusernum) as s8,sum(add9daysusernum) as s9,sum(add10daysusernum) as s10\n" +
				"\t\t\t,sum(add11daysusernum) as s11,sum(add12daysusernum) as s12,sum(add13daysusernum) as s13,sum(add14daysusernum) as s14,sum(add15daysusernum) as s15\n" +
				"\t\t\t,sum(add16daysusernum) as s16,sum(add17daysusernum) as s17,sum(add18daysusernum) as s18,sum(add19daysusernum) as s19,sum(add20daysusernum) as s20\n" +
				"\t\t\t,sum(add21daysusernum) as s21,sum(add22daysusernum) as s22,sum(add23daysusernum) as s23,sum(add24daysusernum) as s24,sum(add25daysusernum) as s25\n" +
				"\t\t\t,sum(add26daysusernum) as s26,sum(add27daysusernum) as s27,sum(add28daysusernum) as s28,sum(add29daysusernum) as s29,sum(add30daysusernum) as s30\n" +
				"\t\tfrom dws_user_viplevel_retained_daily\n" +
				"\t\twhere dt>='"+dt[0]+"' and  dt<=date_add('"+dt[1]+"',30)\n" +
				"\t\tgroup by actdt,regsiteid,regsitename,regchannelid,regchannelname,viplevel,usernum\n" +
				"\t\torder by actdt,regsiteid,regsitename,regchannelid,regchannelname,viplevel,usernum\n" +
				"\t)a1\n" +
				"\twhere actdt>='"+dt[0]+"' and actdt<='"+dt[1]+"'\n" +
				"\tgroup by "+groupitem+"\n" +
				"\torder by "+groupitem+"\n"
		);
		sql.append(") t\n");
		sql.append("order by "+groupitem+"\n");
		JdbcTemplate jdbcTemplate =getJdbcTemplateImpala();
		List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString());

		// 若维度日期，移除 无效日期的 留存0数值  比如看7日留存 那么前天的数据 只有一日留存，因为后面的留存 日期还未发生。
		if(groupitem.equals("actdt")){
			int daynum =DateUtils.compareDayNum(
				DateUtils.addDate(
					DateUtils.StrToDate(dt[0],DateUtils.dayFmt),retainColCount-1
				),
				new Date()
			);
			if(daynum<=0){
				for (int i = 1 ; i < data.size() ; i++) {

					ListIterator<Map.Entry<String, Object>> li =
						new ArrayList<Map.Entry<String, Object>>(data.get(i).entrySet()).listIterator(data.get(i).size());

					for (int j = 0; j < i ; j++) {
						Map.Entry<String, Object> entry = li.previous();
						data.get(i).put(entry.getKey(),"");
					}
				}
			}else{
				for (int i = daynum ; i < data.size() ; i++) {

					ListIterator<Map.Entry<String, Object>> li =
						new ArrayList<Map.Entry<String, Object>>(data.get(i).entrySet()).listIterator(data.get(i).size());

					for (int j = 0; j < i- (daynum-1) ; j++) {
						Map.Entry<String, Object> entry = li.previous();
						data.get(i).put(entry.getKey(),"");
					}
				}
			}
		}

		String[] colNameDic,colTitleDic;
		if(true){
			colNameDic= new String[]{
				"actdt",
				"regsiteid",
				"regchannelid",
				"viplevel",
				"usernum",
				"s1",
				"s2",
				"s3",
				"s4",
				"s5",
				"s6",
				"s7",
				"s8",
				"s9",
				"s10",
				"s11",
				"s12",
				"s13",
				"s14",
				"s15",
				"s16",
				"s17",
				"s18",
				"s19",
				"s20",
				"s21",
				"s22",
				"s23",
				"s24",
				"s25",
				"s26",
				"s27",
				"s28",
				"s29",
				"s30"
			};
			colTitleDic= new String[]{
				"日期",
				"注册站点",
				"注册短渠道",
				"玩家vip等级",
				"玩家总数",
				"第1天",
				"第2天",
				"第3天",
				"第4天",
				"第5天",
				"第6天",
				"第7天",
				"第8天",
				"第9天",
				"第10天",
				"第11天",
				"第12天",
				"第13天",
				"第14天",
				"第15天",
				"第16天",
				"第17天",
				"第18天",
				"第19天",
				"第20天",
				"第21天",
				"第22天",
				"第23天",
				"第24天",
				"第25天",
				"第26天",
				"第27天",
				"第28天",
				"第29天",
				"第30天"
			};
		}
		List colNameList = Arrays.asList(colNameDic);

		boolean loss = false;
		if(map.containsKey("dataType")){
			loss=true;
		}
		// 格式化
		List<String> xAxis = new ArrayList<>();
		List<Map> series = new ArrayList<>();
		String[] colAr = newColNames.split(",");
		List colList = Arrays.asList(colAr);
		for (int i = 0; i < colAr.length ; i++) {
			if(colNameList.indexOf(colAr[i])>-1){
				Map seriesMap = new HashMap();
				seriesMap.put("name",colTitleDic[colNameList.indexOf(colAr[i])]);
				seriesMap.put("data",new ArrayList<Integer>());
				series.add(seriesMap);
			}
		}
		for (int i = 0; i < data.size() ; i++) {
			// 日期 添加week
			if(data.get(i).containsKey("actdt")){
				data.get(i).put(
					"actdt",
					data.get(i).get("actdt")+"(" +
						DateUtils.getWeek(DateUtils.StrToDate(data.get(i).get("actdt").toString(),DateUtils.dayFmt),1)
						+")"
				);
				xAxis.add(data.get(i).get("actdt").toString());
			}
			for (Map.Entry<String, Object> entry :data.get(i).entrySet()) {
				if(newColNames.indexOf( entry.getKey() ) > -1 && !entry.getValue().equals("")){
					if( !entry.getKey().equals("usernum")){
						if(loss){ // 流失
							data.get(i).put(
								entry.getKey(),
								Integer.parseInt(data.get(i).get("usernum").toString())- Integer.parseInt(entry.getValue().toString())
							);
						}
					}
					if(colList.indexOf(entry.getKey())>-1){
						((List)series.get(colList.indexOf(entry.getKey())).get("data")).add(Integer.parseInt(entry.getValue().toString()));
					}
				}
			}
		}

		Map returnMap = new HashMap();
		returnMap.put("xAxis",xAxis);
		returnMap.put("series",series);

		return  returnMap;
	}

	public Map<String,Object> getRetain(Map map){

		// 日期条件
		StringBuffer whereSb = new StringBuffer();
		String[] dt = map.get("dt").toString().split("~");
		whereSb.append(" AND DT BETWEEN '"+dt[0].trim()+"' and '"+dt[1].trim()+"' ");

		// 扩展列
		Map<String,String> extendSelectItemMap = new HashMap();
		extendSelectItemMap.put("regsiteid","concat(cast(regsiteid as string),' - ',regsitename) regsiteid");
		extendSelectItemMap.put("regchannelid","concat(cast(regchannelid as string),' - ',regchannelname) regchannelid");
		Map<String,String> extendGroupItemMap = new HashMap();
		extendGroupItemMap.put("regsiteid","regsiteid,regsitename");
		extendGroupItemMap.put("regchannelid","regchannelid,regchannelname");

		String groupitem = map.get("item").toString();
		String selectitem = map.get("item").toString();
		for (Map.Entry<String, String> entry : extendSelectItemMap.entrySet()) {
			selectitem = selectitem.replace(entry.getKey(),entry.getValue());
			groupitem = groupitem.replace(entry.getKey(),extendGroupItemMap.get(entry.getKey()));
		}

		String target = map.get("target").toString();
		String[] targetDic = new String[]{"1day","3day","7day","14day","30day"};
		String[] selectColAr = new String[]{
			"usernum,s1",
			"usernum,s1,s2,s3",
			"usernum,s1,s2,s3,s4,s5,s6,s7",
			"usernum,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14",
			"usernum,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15,s16,s17,s18,s19,s20,s21,s22,s23,s24,s25,s26,s27,s28,s29,s30"};
		List<String> targetList = Arrays.asList(targetDic);
		StringBuffer sql = new StringBuffer();
		// 维度
		sql.append(
			"select " +selectitem+","
		);

		// 移除无效日期 的留存列 比如看最近7天，选的30日留存 实际没有那么多，最多到第七天。
		String newColNames =selectColAr[targetList.indexOf(target)];
		int validDayNum = DateUtils.compareDayNum(DateUtils.StrToDate(dt[0],DateUtils.dayFmt),new Date());
		int retainColCount = newColNames.split(",").length;
		if(retainColCount > validDayNum){
			for (int i = 0; i < retainColCount - validDayNum ; i++) {
				newColNames=newColNames.substring(0,newColNames.lastIndexOf(","));
			}
		}
		// 指标
		sql.append(
			newColNames + "\n"
		);
		sql.append("from (");
		// 内层 相当于table表名
		sql.append(
			"\tselect "+groupitem+",sum(usernum) usernum,\n" +
				"\t\tsum(s1) as s1,sum(s2) as s2,sum(s3) as s3,sum(s4) as s4,sum(s5) as s5,sum(s6) as s6,sum(s7) as s7,\n" +
				"\t\tsum(s8) as s8,sum(s9) as s9,sum(s10) as s10,sum(s11) as s11,sum(s12) as s12,sum(s13) as s13,sum(s14) as s14,\n" +
				"\t\tsum(s15) as s15,sum(s16) as s16,sum(s17) as s17,sum(s18) as s18,sum(s19) as s19,sum(s20) as s20,sum(s21) as s21,\n" +
				"\t\tsum(s22) as s22,sum(s23) as s23,sum(s24) as s24,sum(s25) as s25,sum(s26) as s26,sum(s27) as s27,sum(s28) as s28,sum(s29) as s29,sum(s30) as s30\n" +
				"\tfrom(\n" +
				"\t\tselect actdt,regsiteid,regsitename,regchannelid,regchannelname,viplevel,usernum\n" +
				"\t\t\t,sum(add1dayusernum) as s1,sum(add2daysusernum) as s2,sum(add3daysusernum) as s3,sum(add4daysusernum) as s4,sum(add5daysusernum) as s5\n" +
				"\t\t\t,sum(add6daysusernum) as s6,sum(add7daysusernum) as s7,sum(add8daysusernum) as s8,sum(add9daysusernum) as s9,sum(add10daysusernum) as s10\n" +
				"\t\t\t,sum(add11daysusernum) as s11,sum(add12daysusernum) as s12,sum(add13daysusernum) as s13,sum(add14daysusernum) as s14,sum(add15daysusernum) as s15\n" +
				"\t\t\t,sum(add16daysusernum) as s16,sum(add17daysusernum) as s17,sum(add18daysusernum) as s18,sum(add19daysusernum) as s19,sum(add20daysusernum) as s20\n" +
				"\t\t\t,sum(add21daysusernum) as s21,sum(add22daysusernum) as s22,sum(add23daysusernum) as s23,sum(add24daysusernum) as s24,sum(add25daysusernum) as s25\n" +
				"\t\t\t,sum(add26daysusernum) as s26,sum(add27daysusernum) as s27,sum(add28daysusernum) as s28,sum(add29daysusernum) as s29,sum(add30daysusernum) as s30\n" +
				"\t\tfrom dws_user_viplevel_retained_daily\n" +
				"\t\twhere dt>='"+dt[0]+"' and  dt<=date_add('"+dt[1]+"',30)\n" +
				"\t\tgroup by actdt,regsiteid,regsitename,regchannelid,regchannelname,viplevel,usernum\n" +
				"\t\torder by actdt,regsiteid,regsitename,regchannelid,regchannelname,viplevel,usernum\n" +
				"\t)a1\n" +
				"\twhere actdt>='"+dt[0]+"' and actdt<='"+dt[1]+"'\n" +
				"\tgroup by "+groupitem+"\n" +
				"\torder by "+groupitem+"\n"
		);
		sql.append(") t\n");
		sql.append("order by "+groupitem+"\n");
		JdbcTemplate jdbcTemplate =getJdbcTemplateImpala();
		List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString());

		// 若维度日期，移除 无效日期的 留存0数值  比如看7日留存 那么前天的数据 只有一日留存，因为后面的留存 日期还未发生。
		if(groupitem.equals("actdt")){
			int daynum =DateUtils.compareDayNum(
				DateUtils.addDate(
					DateUtils.StrToDate(dt[0],DateUtils.dayFmt),retainColCount-1
				),
				new Date()
			);
			if(daynum<=0){
				for (int i = 1 ; i < data.size() ; i++) {

					ListIterator<Map.Entry<String, Object>> li =
						new ArrayList<Map.Entry<String, Object>>(data.get(i).entrySet()).listIterator(data.get(i).size());

					for (int j = 0; j < i ; j++) {
						Map.Entry<String, Object> entry = li.previous();
						data.get(i).put(entry.getKey(),"");
					}
				}
			}else{
				for (int i = daynum ; i < data.size() ; i++) {

					ListIterator<Map.Entry<String, Object>> li =
						new ArrayList<Map.Entry<String, Object>>(data.get(i).entrySet()).listIterator(data.get(i).size());

					for (int j = 0; j < i- (daynum-1) ; j++) {
						Map.Entry<String, Object> entry = li.previous();
						data.get(i).put(entry.getKey(),"");
					}
				}
			}
		}

		boolean loss = false;
		if(map.containsKey("dataType")){
			loss=true;
		}
		// 追加百分比
		for (int i = 0; i < data.size() ; i++) {
			for (Map.Entry<String, Object> entry :data.get(i).entrySet()) {
				if(newColNames.indexOf( entry.getKey() ) > -1 && !entry.getValue().equals("")){
					if(!entry.getKey().equals("usernum")){

						if(loss){ // 流失
							data.get(i).put(
								entry.getKey(),
								Integer.parseInt(data.get(i).get("usernum").toString())- Integer.parseInt(entry.getValue().toString())
							);
						}

						// 百分比
						data.get(i).put(
							entry.getKey(),
							entry.getValue()+"(" +
								String.format("%.2f",
									(Double.parseDouble(entry.getValue().toString()) /
										Double.parseDouble(data.get(i).get("usernum").toString()) ) * 100
								) + "%)"
						);
					}
				}
			}
		}

		String[] tableNameAr =(map.get("item").toString()+","+newColNames).split(",");
		String[] colNameDic,colTitleDic;
		if(true){
			colNameDic= new String[]{
				"actdt",
				"regsiteid",
				"regchannelid",
				"viplevel",
				"usernum",
				"s1",
				"s2",
				"s3",
				"s4",
				"s5",
				"s6",
				"s7",
				"s8",
				"s9",
				"s10",
				"s11",
				"s12",
				"s13",
				"s14",
				"s15",
				"s16",
				"s17",
				"s18",
				"s19",
				"s20",
				"s21",
				"s22",
				"s23",
				"s24",
				"s25",
				"s26",
				"s27",
				"s28",
				"s29",
				"s30"
			};
			colTitleDic= new String[]{
				"日期",
				"注册站点",
				"注册短渠道",
				"玩家vip等级",
				"玩家总数",
				"第1天",
				"第2天",
				"第3天",
				"第4天",
				"第5天",
				"第6天",
				"第7天",
				"第8天",
				"第9天",
				"第10天",
				"第11天",
				"第12天",
				"第13天",
				"第14天",
				"第15天",
				"第16天",
				"第17天",
				"第18天",
				"第19天",
				"第20天",
				"第21天",
				"第22天",
				"第23天",
				"第24天",
				"第25天",
				"第26天",
				"第27天",
				"第28天",
				"第29天",
				"第30天"
			};
		}

		String[] tableTitleAr = new String[tableNameAr.length];
		for (int i = 0; i < tableNameAr.length ; i++) {
			int index = Arrays.asList(colNameDic).indexOf(tableNameAr[i]);
			if( index > -1){
				tableTitleAr[i] = colTitleDic[index];
			}
		}
		Map returnMap = new HashMap();
		returnMap.put("tableData",data);
		returnMap.put("tableTitle",tableTitleAr);
		returnMap.put("tableName",tableNameAr);

		return  returnMap;
	}


	public JdbcTemplate getJdbcTemplate(){
		DriverManagerDataSource dataSource=new DriverManagerDataSource();
		dataSource.setDriverClassName("org.apache.kylin.jdbc.Driver");
		dataSource.setUrl("jdbc:kylin://47.111.189.12:7070/datatalk");
		dataSource.setUsername("ADMIN");
		dataSource.setPassword("KYLIN");
		return new JdbcTemplate(dataSource);
	}

	public JdbcTemplate getJdbcTemplateImpala(){
		DriverManagerDataSource dataSource=new DriverManagerDataSource();
		dataSource.setDriverClassName("com.cloudera.impala.jdbc41.Driver");
		dataSource.setUrl("jdbc:impala://47.99.111.178:21050/datatalk");
		dataSource.setUsername("");
		dataSource.setPassword("");
		return new JdbcTemplate(dataSource);
	}
}
