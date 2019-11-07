package org.jeecg.modules.bigdata.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.bigdata.service.IKylinService;
import org.jeecg.modules.bigdata.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
* @Description: 报表分析
* @Author: jeecg-boot
* @Date:   2019-10-17
* @Version: V1.0
*/
@Slf4j
@Api(tags="报表分析")
@RestController
@RequestMapping("/bigdata/report")
public class ReportController {
	@Autowired
	private IReportService reportService;

	@AutoLog(value = "报表分析图表数据查询")
	@ApiOperation(value="报表分析图表数据查询", notes="报表分析图表数据查询")
	@PostMapping(value = "/line")
	public Result<Map> queryReportChart(@RequestBody Map<String,Object> param) {
		Result<Map> result = new Result<Map>();
		Map rmap = reportService.queryReportChart(param);
		result.setResult(rmap);
		result.setSuccess(true);
		return result;
	}

	@AutoLog(value = "报表分析报表数据查询")
	@ApiOperation(value="报表分析报表数据查询", notes="报表分析报表数据查询")
	@PostMapping(value = "/table")
	public Result<Map> queryReportTable(@RequestBody Map<String,Object> param) {
		Result<Map> result = new Result<Map>();
		Map rmap = reportService.queryReportTable(param);
		result.setResult(rmap);
		result.setSuccess(true);
		return result;
	}
}
