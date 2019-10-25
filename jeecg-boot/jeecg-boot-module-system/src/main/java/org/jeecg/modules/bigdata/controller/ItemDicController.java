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
import org.jeecg.modules.bigdata.entity.ItemDic;
import org.jeecg.modules.bigdata.service.IItemDicService;
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
 * @Description: 维度配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
@Slf4j
@Api(tags="维度配置表")
@RestController
@RequestMapping("/bigdata/itemDic")
public class ItemDicController {
	@Autowired
	private IItemDicService itemDicService;
	
	/**
	  * 分页列表查询
	 * @param itemDic
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "维度配置表-分页列表查询")
	@ApiOperation(value="维度配置表-分页列表查询", notes="维度配置表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<ItemDic>> queryPageList(ItemDic itemDic,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<ItemDic>> result = new Result<IPage<ItemDic>>();
		QueryWrapper<ItemDic> queryWrapper = QueryGenerator.initQueryWrapper(itemDic, req.getParameterMap());
		Page<ItemDic> page = new Page<ItemDic>(pageNo, pageSize);
		IPage<ItemDic> pageList = itemDicService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	 @AutoLog(value = "根据事件ID获取对应的维度")
	 @ApiOperation(value="根据事件ID获取对应的维度", notes="根据事件ID获取对应的维度")
	 @GetMapping(value = "/getEventItems")
	 public Result<List<Map>> getEventItems(@RequestParam(name="eventId") Integer eventId) {
		 Result<List<Map>> result = new Result<List<Map>>();
		 List<Map> list = itemDicService.getEventItems(eventId);
		 result.setSuccess(true);
		 result.setResult(list);
		 return result;
	 }
	
	/**
	  *   添加
	 * @param itemDic
	 * @return
	 */
	@AutoLog(value = "维度配置表-添加")
	@ApiOperation(value="维度配置表-添加", notes="维度配置表-添加")
	@PostMapping(value = "/add")
	public Result<ItemDic> add(@RequestBody ItemDic itemDic) {
		Result<ItemDic> result = new Result<ItemDic>();
		try {
			itemDicService.save(itemDic);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  *  编辑
	 * @param itemDic
	 * @return
	 */
	@AutoLog(value = "维度配置表-编辑")
	@ApiOperation(value="维度配置表-编辑", notes="维度配置表-编辑")
	@PutMapping(value = "/edit")
	public Result<ItemDic> edit(@RequestBody ItemDic itemDic) {
		Result<ItemDic> result = new Result<ItemDic>();
		ItemDic itemDicEntity = itemDicService.getById(itemDic.getId());
		if(itemDicEntity==null) {
			result.error500("未找到对应实体");
		}else {
			boolean ok = itemDicService.updateById(itemDic);
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
	@AutoLog(value = "维度配置表-通过id删除")
	@ApiOperation(value="维度配置表-通过id删除", notes="维度配置表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		try {
			itemDicService.removeById(id);
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
	@AutoLog(value = "维度配置表-批量删除")
	@ApiOperation(value="维度配置表-批量删除", notes="维度配置表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<ItemDic> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<ItemDic> result = new Result<ItemDic>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("参数不识别！");
		}else {
			this.itemDicService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}
	
	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@AutoLog(value = "维度配置表-通过id查询")
	@ApiOperation(value="维度配置表-通过id查询", notes="维度配置表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<ItemDic> queryById(@RequestParam(name="id",required=true) String id) {
		Result<ItemDic> result = new Result<ItemDic>();
		ItemDic itemDic = itemDicService.getById(id);
		if(itemDic==null) {
			result.error500("未找到对应实体");
		}else {
			result.setResult(itemDic);
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
      QueryWrapper<ItemDic> queryWrapper = null;
      try {
          String paramsStr = request.getParameter("paramsStr");
          if (oConvertUtils.isNotEmpty(paramsStr)) {
              String deString = URLDecoder.decode(paramsStr, "UTF-8");
              ItemDic itemDic = JSON.parseObject(deString, ItemDic.class);
              queryWrapper = QueryGenerator.initQueryWrapper(itemDic, request.getParameterMap());
          }
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }

      //Step.2 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      List<ItemDic> pageList = itemDicService.list(queryWrapper);
      //导出文件名称
      mv.addObject(NormalExcelConstants.FILE_NAME, "维度配置表列表");
      mv.addObject(NormalExcelConstants.CLASS, ItemDic.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("维度配置表列表数据", "导出人:Jeecg", "导出信息"));
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
              List<ItemDic> listItemDics = ExcelImportUtil.importExcel(file.getInputStream(), ItemDic.class, params);
              itemDicService.saveBatch(listItemDics);
              return Result.ok("文件导入成功！数据行数:" + listItemDics.size());
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
