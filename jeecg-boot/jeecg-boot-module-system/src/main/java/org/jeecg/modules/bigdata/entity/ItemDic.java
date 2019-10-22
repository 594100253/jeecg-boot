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
 * @Description: 维度配置表
 * @Author: jeecg-boot
 * @Date:   2019-10-18
 * @Version: V1.0
 */
@Data
@TableName("bu_item_dic")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="bu_item_dic对象", description="维度配置表")
public class ItemDic {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private java.lang.Integer id;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
	private java.lang.Integer sortNo;
	/**状态 1：选中*/
	@Excel(name = "状态 1：选中", width = 15)
    @ApiModelProperty(value = "状态 1：选中")
	private java.lang.Integer isSelect;
	/**状态 1：选中*/
	@Excel(name = "状态 1：选中", width = 15)
    @ApiModelProperty(value = "状态 1：选中")
	private java.lang.Integer ifChecked;
	/**是否筛选 1：筛选*/
	@Excel(name = "是否筛选 1：筛选", width = 15)
    @ApiModelProperty(value = "是否筛选 1：筛选")
	private java.lang.Integer isSearch;
	/**搜索提示输入*/
	@Excel(name = "搜索提示输入", width = 15)
    @ApiModelProperty(value = "搜索提示输入")
	private java.lang.String searchPlaceholder;
	/**搜索时的列名*/
	@Excel(name = "搜索时的列名", width = 15)
    @ApiModelProperty(value = "搜索时的列名")
	private java.lang.String searchColumn;
	/**字典地址*/
	@Excel(name = "字典地址", width = 15)
    @ApiModelProperty(value = "字典地址")
	private java.lang.String dicUrl;
	/**维度分类*/
	@Excel(name = "维度分类", width = 15)
    @ApiModelProperty(value = "维度分类")
	private java.lang.String itemType;
	/**维度名称*/
	@Excel(name = "维度名称", width = 15)
    @ApiModelProperty(value = "维度名称")
	private java.lang.String itemName;
	/**若为X轴的维度单位，（db_column_name的数值+item_x_unit（如7+月））*/
	@Excel(name = "若为X轴的维度单位，（db_column_name的数值+item_x_unit（如7+月））", width = 15)
    @ApiModelProperty(value = "若为X轴的维度单位，（db_column_name的数值+item_x_unit（如7+月））")
	private java.lang.String itemXUnit;
	/**最小列宽*/
	@Excel(name = "最小列宽", width = 15)
    @ApiModelProperty(value = "最小列宽")
	private java.lang.Integer itemMinWidth;
	/**维度-placeholder*/
	@Excel(name = "维度-placeholder", width = 15)
    @ApiModelProperty(value = "维度-placeholder")
	private java.lang.String itemPlaceholder;
	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @ApiModelProperty(value = "字段名")
	private java.lang.String dbColumnName;
	/**字段类型*/
	@Excel(name = "字段类型", width = 15)
    @ApiModelProperty(value = "字段类型")
	private java.lang.String dbColumnType;
	/**字段执行sql使用*/
	@Excel(name = "字段执行sql使用", width = 15)
    @ApiModelProperty(value = "字段执行sql使用")
	private java.lang.String dbColumnSql;
	/**是否必须：1必须，0无需；*/
	@Excel(name = "是否必须：1必须，0无需；", width = 15)
    @ApiModelProperty(value = "是否必须：1必须，0无需；")
	private java.lang.Integer isMust;
	/**是否层级：0否；其他代表本层级顶级的id*/
	@Excel(name = "是否层级：0否；其他代表本层级顶级的id", width = 15)
    @ApiModelProperty(value = "是否层级：0否；其他代表本层级顶级的id")
	private java.lang.Integer isHierarchy;
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
}
