package org.jeecg.modules.bigdata.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.bigdata.entity.EventDic;
import org.jeecg.modules.bigdata.entity.ItemDic;
import org.jeecg.modules.bigdata.entity.TargetDic;
import org.jeecg.modules.bigdata.mapper.EventDicMapper;
import org.jeecg.modules.bigdata.service.IEventDicService;
import org.jeecg.modules.bigdata.service.IKylinService;
import org.jeecg.modules.bigdata.service.IReportService;
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
public class ReportServiceImpl extends ServiceImpl<EventDicMapper, EventDic> implements IReportService {

	@Autowired
	private EventDicMapper eventDicMapper;

	@Autowired
	private IEventDicService eventDicService;
	@Autowired
	private ITargetDicService targetDicService;
	@Autowired
	private ItemDicServiceImpl itemDicService;

	// 单事件 图表
	public Map<String,Object> queryReportChart(Map map) {

		Map returnMap = new HashMap();
		// 获取事件
		StringBuffer selectSql = new StringBuffer();
		Integer eventId = Integer.parseInt(map.get("eventId").toString());
		EventDic eventDic= eventDicService.getById(eventId);

		// x轴
		String xAxisName = (String) map.get("xName");

		// 列
		StringBuffer select = new StringBuffer();

		List<String> target = (List) map.get("target");
		String[] fields = new String[target.size()];
		for (int i = 0; i < target.size() ; i++) {

			TargetDic targetDic = targetDicService.getById(target.get(i));
			if(select.length()>0){
				select.append(",");
			}
			select.append(
				targetDic.getTargetNameFunction() + " as " +" '"+targetDic.getTargetName()+"' "
			);
			fields[i] = targetDic.getTargetName();
		}

		// 条件
		StringBuffer whereSb = new StringBuffer("\nwhere 1 = 1 ");
		String[] dt = map.get("dt").toString().split("~");
		whereSb.append(" AND DT BETWEEN '"+dt[0].trim()+"' and '"+dt[1].trim()+"' ");

		// 分组
		StringBuffer groupBy = new StringBuffer( "\ngroup by " + xAxisName);

		// 排序
		StringBuffer orderBy = new StringBuffer( "\norder by " + xAxisName);

		StringBuffer sql = new StringBuffer();
		sql.append(
			"select \n"+
			"\tconcat(cast(timeyear as string),'-',cast(timemonth as string),'-',cast(timeday as string)) as x," + select + "\n"+
			"from " + eventDic.getEventTable() + whereSb + groupBy
		);
		JdbcTemplate jdbcTemplate = getJdbcTemplateImpala();

		List<Map<String, Object>> data =jdbcTemplate.queryForList(sql.toString());

		returnMap.put("data",data);
		returnMap.put("fields",fields);
		return returnMap;
	}

	// 单事件 表格
	public Map<String, Object> queryReportTable(Map map) {

		Map returnMap = new HashMap();

		// 获取事件
		StringBuffer selectSql = new StringBuffer();
		Integer eventId = Integer.parseInt(map.get("eventId").toString());
		EventDic eventDic= eventDicService.getById(eventId);

		List<String> itemIds = (List) map.get("item");
		List<String> targetIds = (List) map.get("target");

		// 根据 事件、维度和指标的条件，查询对应的 title（用于显示列名）、name(列key)、sqlname(sql的select列和groupby的列)
		Map getHierarchyItemMap = new HashMap();
		getHierarchyItemMap.put("eventId",eventId);
		getHierarchyItemMap.put("itemIds",StringUtils.join(itemIds.toArray(),","));
		getHierarchyItemMap.put("targetIds",StringUtils.join(targetIds.toArray(),","));
		Map concatMap =itemDicService.getConcatColumn(getHierarchyItemMap);

		// Table列宽
		String[] widths = concatMap.get("width").toString().split(",");
		returnMap.put("widths",widths);

		// Table列标题
		String[] tableTitle = concatMap.get("tableTitle").toString().split(",");
		returnMap.put("tableTitle",tableTitle);

		// Table列名
		String[] tableName = concatMap.get("tableName").toString().split(",");
		returnMap.put("tableName",tableName);

		// Sql列
		StringBuffer select = new StringBuffer();
		select.append(concatMap.get("selectName").toString());

		// Sql条件
		StringBuffer whereSb = new StringBuffer("\nwhere 1 = 1 ");
		String[] dt = map.get("dt").toString().split("~");
		whereSb.append(" AND DT BETWEEN '"+dt[0].trim()+"' and '"+dt[1].trim()+"' ");

		// Sql分组
		StringBuffer groupBy = new StringBuffer( "\ngroup by ");
		groupBy.append(concatMap.get("groupName").toString());

		// Sql排序
		StringBuffer orderBy = new StringBuffer( "\norder by " + itemIds);

		StringBuffer sql = new StringBuffer();
		sql.append(
			"select \n"+
				"\t"+ select + "\n"+
			"from " + eventDic.getEventTable() + whereSb + groupBy
		);

		JdbcTemplate jdbcTemplate = getJdbcTemplateImpala();
		List<Map<String, Object>> data =jdbcTemplate.queryForList(sql.toString());
		returnMap.put("records",data);
		return returnMap;
	}

	// 留存
	public Map<String,Object> getRetainChart(Map map){

		Map returnMap = new HashMap();
		

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

		String target = map.get("targetName").toString();
		String[] targetDic = new String[]{"1day","3day","7day","14day","30day"};
		String[] selectColAr = new String[]{
			"usernum as '玩家总数',decode(sign(s1),0,null,1,s1) as '第1天'",
			"usernum as '玩家总数',decode(sign(s1),0,null,1,s1) as '第1天',decode(sign(s2),0,null,1,s2) as '第2天',decode(sign(s3),0,null,1,s3) as '第3天'",
			"usernum as '玩家总数',decode(sign(s1),0,null,1,s1) as '第1天',decode(sign(s2),0,null,1,s2) as '第2天',decode(sign(s3),0,null,1,s3) as '第3天'," +
				"decode(sign(s4),0,null,1,s4) as '第4天',decode(sign(s5),0,null,1,s5) as '第5天',decode(sign(s6),0,null,1,s6) as '第6天'," +
				"decode(sign(s7),0,null,1,s7) as '第7天'",
			"usernum as '玩家总数',decode(sign(s1),0,null,1,s1) as '第1天',decode(sign(s2),0,null,1,s2) as '第2天',decode(sign(s3),0,null,1,s3) as '第3天'," +
				"decode(sign(s4),0,null,1,s4) as '第4天',decode(sign(s5),0,null,1,s5) as '第5天',decode(sign(s6),0,null,1,s6) as '第6天'," +
				"decode(sign(s7),0,null,1,s7) as '第7天',decode(sign(s8),0,null,1,s8) as '第8天',decode(sign(s9),0,null,1,s9) as '第9天'," +
				"decode(sign(s10),0,null,1,s10) as '第10天',decode(sign(s11),0,null,1,s11) as '第11天',decode(sign(s12),0,null,1,s12) as '第12天'," +
				"decode(sign(s13),0,null,1,s13) as '第13天',decode(sign(s14),0,null,1,s14) as '第14天'",
			"usernum as '玩家总数',decode(sign(s1),0,null,1,s1) as '第1天',decode(sign(s2),0,null,1,s2) as '第2天',decode(sign(s3),0,null,1,s3) as '第3天'," +
				"decode(sign(s4),0,null,1,s4) as '第4天',decode(sign(s5),0,null,1,s5) as '第5天',decode(sign(s6),0,null,1,s6) as '第6天'," +
				"decode(sign(s7),0,null,1,s7) as '第7天',decode(sign(s8),0,null,1,s8) as '第8天',decode(sign(s9),0,null,1,s9) as '第9天'," +
				"decode(sign(s10),0,null,1,s10) as '第10天',decode(sign(s11),0,null,1,s11) as '第11天',decode(sign(s12),0,null,1,s12) as '第12天'," +
				"decode(sign(s13),0,null,1,s13) as '第13天',decode(sign(s14),0,null,1,s14) as '第14天',decode(sign(s14),0,null,1,s14) as '第14天'," +
				"decode(sign(s15),0,null,1,s15) as '第15天',decode(sign(s16),0,null,1,s16) as '第16天',decode(sign(s17),0,null,1,s17) as '第17天'," +
				"decode(sign(s18),0,null,1,s18) as '第18天',decode(sign(s19),0,null,1,s19) as '第19天',decode(sign(s20),0,null,1,s20) as '第20天'," +
				"decode(sign(s21),0,null,1,s21) as '第21天',decode(sign(s22),0,null,1,s22) as '第14天',decode(sign(s23),0,null,1,s23) as '第23天'," +
				"decode(sign(s24),0,null,1,s24) as '第24天',decode(sign(s25),0,null,1,s25) as '第25天',decode(sign(s26),0,null,1,s26) as '第26天'," +
				"decode(sign(s27),0,null,1,s27) as '第27天',decode(sign(s28),0,null,1,s28) as '第28天',decode(sign(s30),0,null,1,s30) as '第30天'"
		};
		String[] fields = new String[]{
			"玩家总数,第1天",
			"玩家总数,第1天,第2天,第3天",
			"玩家总数,第1天,第2天,第3天,第4天,第5天,第6天,第7天",
			"玩家总数,第1天,第2天,第3天,第4天,第5天,第6天,第7天,第8天,第9天,第10天,第11天,第12天,第13天,第14天",
			"玩家总数,第1天,第2天,第3天,第4天,第5天,第6天,第7天,第8天,第9天,第10天,第11天,第12天,第13天,第14天,第15天,第16天,第17天,第18天,第19天,第20天,第21天," +
				"第22天,第23天,第24天,第25天,第26天,第27天,第28天,第29天,第30天",
		};
		List<String> targetList = Arrays.asList(targetDic);
		StringBuffer sql = new StringBuffer();
		// 维度
		sql.append(
			"select " +selectitem+","
		);

		String newColNames =selectColAr[targetList.indexOf(target)];
		returnMap.put("fields",fields[targetList.indexOf(target)].split(","));
		// 移除无效日期 的留存列 比如看最近7天，选的30日留存 实际没有那么多，最多到第七天。
//		int validDayNum = DateUtils.compareDayNum(DateUtils.StrToDate(dt[0],DateUtils.dayFmt),new Date());
//		int retainColCount = newColNames.split(",").length;
//		if(retainColCount > validDayNum){
//			for (int i = 0; i < retainColCount - validDayNum ; i++) {
//				newColNames=newColNames.substring(0,newColNames.lastIndexOf(","));
//			}
//		}

		// 留存
		if("loss".equals(map.get("retainType"))){
			newColNames = newColNames.replaceAll("[)],0,null,1,s","),0,null,1,usernum-s");
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

		returnMap.put("data",data);

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

		String target = map.get("targetName").toString();
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

		String selectColNames ;
		// 留存
		if("loss".equals(map.get("retainType"))){
			String[] colsAr = newColNames.split(",");
			for (int i = 1; i < colsAr.length; i++) {
				colsAr[i] = "usernum-"+colsAr[i] + " as "+colsAr[i];
			}
			selectColNames = StringUtils.join(colsAr,",");
		}else{
			selectColNames = newColNames;
		}

		// 指标
		sql.append(
			selectColNames + "\n"
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
		dataSource.setUrl("jdbc:impala://47.99.111.178:21050/datatalk/;auth=noSasl");
		dataSource.setUsername("");
		dataSource.setPassword("");
		return new JdbcTemplate(dataSource);
	}
}
