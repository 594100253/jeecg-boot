package org.jeecg.modules.bigdata.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 事件配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-17
 * @Version: V1.0
 */
@Data
@TableName("bu_event_dic")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="bu_event_dic对象", description="事件配置表")
public class EventDic {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private java.lang.Integer id;
	/**状态 0：其他分析 1：事件分析*/
	@Excel(name = "状态 0：其他分析 1：事件分析", width = 15)
    @ApiModelProperty(value = "状态 0：其他分析 1：事件分析")
	private java.lang.Integer eventStatus;
	/**事件分类*/
	@Excel(name = "事件分类", width = 15)
    @ApiModelProperty(value = "事件分类")
	private java.lang.String eventType;
	/**事件名称*/
	@Excel(name = "事件名称", width = 15)
    @ApiModelProperty(value = "事件名称")
	private java.lang.String eventName;
	/**事件对应表*/
	@Excel(name = "事件对应表", width = 15)
    @ApiModelProperty(value = "事件对应表")
	private java.lang.String eventTable;
	/**创建者ID*/
	@Excel(name = "创建者ID", width = 15)
    @ApiModelProperty(value = "创建者ID")
	private java.lang.Integer createUid;
	/**修改者ID*/
	@Excel(name = "修改者ID", width = 15)
    @ApiModelProperty(value = "修改者ID")
	private java.lang.Integer updateUid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**修改时间*/
	@Excel(name = "修改时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**状态 0：禁用 1：正常*/
	@Excel(name = "状态 0：禁用 1：正常", width = 15)
    @ApiModelProperty(value = "状态 0：禁用 1：正常")
	private java.lang.Integer status;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
	private java.lang.Integer sortNo;
}
