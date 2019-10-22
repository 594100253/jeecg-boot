package org.jeecg.modules.bigdata.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.bigdata.entity.EventDic;
import org.jeecg.modules.bigdata.service.IEventDicService;
import org.jeecg.modules.bigdata.service.IKylinService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @Description: 事件配置表
* @Author: jeecg-boot
* @Date:   2019-10-17
* @Version: V1.0
*/
@Slf4j
@Api(tags="数据分析")
@RestController
@RequestMapping("/bigdata/kylin")
public class KylinController {
	@Autowired
	private IKylinService kylinService;

	@AutoLog(value = "事件配置表-分页列表查询")
	@ApiOperation(value="事件配置表-分页列表查询", notes="事件配置表-分页列表查询")
	@PostMapping(value = "/line")
	public Result<Map> queryPageList(@RequestBody Map<String,Object> param) {
		Result<Map> result = new Result<Map>();
		Map rmap = kylinService.querySingleEventChart(param);
		result.setResult(rmap);
		result.setSuccess(true);
		return result;
	}
}
