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
 * @Description: 指标配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
@Data
@TableName("bu_target_dic")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="bu_target_dic对象", description="指标配置表")
public class TargetDic {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private java.lang.Integer id;
	/**指标分类*/
	@Excel(name = "指标分类", width = 15)
    @ApiModelProperty(value = "指标分类")
	private java.lang.String targetType;
	/**格式化显示，PCT：前一天的百分比；*/
	@Excel(name = "格式化显示，PCT：前一天的百分比；", width = 15)
    @ApiModelProperty(value = "格式化显示，PCT：前一天的百分比；")
	private java.lang.String ifFormat;
	/**状态 1：选中*/
	@Excel(name = "状态 1：选中", width = 15)
    @ApiModelProperty(value = "状态 1：选中")
	private java.lang.Integer ifChecked;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
	private java.lang.Integer sortNo;
	/**状态 1：选中*/
	@Excel(name = "状态 1：选中", width = 15)
    @ApiModelProperty(value = "状态 1：选中")
	private java.lang.Integer isSelect;
	/**指标名称*/
	@Excel(name = "指标名称", width = 15)
    @ApiModelProperty(value = "指标名称")
	private java.lang.String targetName;
	/**指标对应列名*/
	@Excel(name = "指标对应列名", width = 15)
    @ApiModelProperty(value = "指标对应列名")
	private java.lang.String targetNameColumn;
	/**指标别名*/
	@Excel(name = "指标别名", width = 15)
    @ApiModelProperty(value = "指标别名")
	private java.lang.String targetAlias;
	/**指标对应列函数*/
	@Excel(name = "指标对应列函数", width = 15)
    @ApiModelProperty(value = "指标对应列函数")
	private java.lang.String targetNameFunction;
	/**指标对应条件*/
	@Excel(name = "指标对应条件", width = 15)
    @ApiModelProperty(value = "指标对应条件")
	private java.lang.String targetNameWhere;
	/**最小列宽*/
	@Excel(name = "最小列宽", width = 15)
    @ApiModelProperty(value = "最小列宽")
	private java.lang.Integer targetMinWidth;
	/**状态 0：禁用 1：正常*/
	@Excel(name = "状态 0：禁用 1：正常", width = 15)
    @ApiModelProperty(value = "状态 0：禁用 1：正常")
	private java.lang.Integer status;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**创建者ID*/
	@Excel(name = "创建者ID", width = 15)
    @ApiModelProperty(value = "创建者ID")
	private java.lang.Integer createUid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**修改者ID*/
	@Excel(name = "修改者ID", width = 15)
    @ApiModelProperty(value = "修改者ID")
	private java.lang.Integer updateUid;
	/**修改时间*/
	@Excel(name = "修改时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
}
