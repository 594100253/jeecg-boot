package org.jeecg.modules.bigdata.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.bigdata.entity.EventItem;
import org.jeecg.modules.bigdata.service.IEventItemService;
import java.util.Date;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

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
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 事件维度关系表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="事件维度关系表")
@RestController
@RequestMapping("/bigdata/eventItem")
public class EventItemController {
	@Autowired
	private IEventItemService eventItemService;
	
	/**
	  * 分页列表查询
	 * @param eventItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "事件维度关系表-分页列表查询")
	@ApiOperation(value="事件维度关系表-分页列表查询", notes="事件维度关系表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<EventItem>> queryPageList(EventItem eventItem,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<EventItem>> result = new Result<IPage<EventItem>>();
		QueryWrapper<EventItem> queryWrapper = QueryGenerator.initQueryWrapper(eventItem, req.getParameterMap());
		Page<EventItem> page = new Page<EventItem>(pageNo, pageSize);
		IPage<EventItem> pageList = eventItemService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	  *   添加
	 * @param eventItem
	 * @return
	 */
	@AutoLog(value = "事件维度关系表-添加")
	@ApiOperation(value="事件维度关系表-添加", notes="事件维度关系表-添加")
	@PostMapping(value = "/add")
	public Result<EventItem> add(@RequestBody EventItem eventItem) {
		Result<EventItem> result = new Result<EventItem>();
		try {
			eventItemService.save(eventItem);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  *  编辑
	 * @param eventItem
	 * @return
	 */
	@AutoLog(value = "事件维度关系表-编辑")
	@ApiOperation(value="事件维度关系表-编辑", notes="事件维度关系表-编辑")
	@PutMapping(value = "/edit")
	public Result<EventItem> edit(@RequestBody EventItem eventItem) {
		Result<EventItem> result = new Result<EventItem>();
		EventItem eventItemEntity = eventItemService.getById(eventItem.getId());
		if(eventItemEntity==null) {
			result.error500("未找到对应实体");
		}else {
			boolean ok = eventItemService.updateById(eventItem);
			//TODO 返回false说明什么？
			if(ok) {
				result.success("修改成功!");
			}
		}
		
		return result;
	}
	
	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	@AutoLog(value = "事件维度关系表-通过id删除")
	@ApiOperation(value="事件维度关系表-通过id删除", notes="事件维度关系表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		try {
			eventItemService.removeById(id);
		} catch (Exception e) {
			log.error("删除失败",e.getMessage());
			return Result.error("删除失败!");
		}
		return Result.ok("删除成功!");
	}
	
	/**
	  *  批量删除
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "事件维度关系表-批量删除")
	@ApiOperation(value="事件维度关系表-批量删除", notes="事件维度关系表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<EventItem> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<EventItem> result = new Result<EventItem>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("参数不识别！");
		}else {
			this.eventItemService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}
	
	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@AutoLog(value = "事件维度关系表-通过id查询")
	@ApiOperation(value="事件维度关系表-通过id查询", notes="事件维度关系表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<EventItem> queryById(@RequestParam(name="id",required=true) String id) {
		Result<EventItem> result = new Result<EventItem>();
		EventItem eventItem = eventItemService.getById(id);
		if(eventItem==null) {
			result.error500("未找到对应实体");
		}else {
			result.setResult(eventItem);
			result.setSuccess(true);
		}
		return result;
	}

  /**
      * 导出excel
   *
   * @param request
   * @param response
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, HttpServletResponse response) {
      // Step.1 组装查询条件
      QueryWrapper<EventItem> queryWrapper = null;
      try {
          String paramsStr = request.getParameter("paramsStr");
          if (oConvertUtils.isNotEmpty(paramsStr)) {
              String deString = URLDecoder.decode(paramsStr, "UTF-8");
              EventItem eventItem = JSON.parseObject(deString, EventItem.class);
              queryWrapper = QueryGenerator.initQueryWrapper(eventItem, request.getParameterMap());
          }
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }

      //Step.2 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      List<EventItem> pageList = eventItemService.list(queryWrapper);
      //导出文件名称
      mv.addObject(NormalExcelConstants.FILE_NAME, "事件维度关系表列表");
      mv.addObject(NormalExcelConstants.CLASS, EventItem.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("事件维度关系表列表数据", "导出人:Jeecg", "导出信息"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      return mv;
  }

  /**
      * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
      for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          MultipartFile file = entity.getValue();// 获取上传文件对象
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<EventItem> listEventItems = ExcelImportUtil.importExcel(file.getInputStream(), EventItem.class, params);
              eventItemService.saveBatch(listEventItems);
              return Result.ok("文件导入成功！数据行数:" + listEventItems.size());
          } catch (Exception e) {
              log.error(e.getMessage(),e);
              return Result.error("文件导入失败:"+e.getMessage());
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return Result.ok("文件导入失败！");
  }

}
