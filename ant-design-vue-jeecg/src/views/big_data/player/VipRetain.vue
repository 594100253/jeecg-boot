<template>
  <div>
    <a-card :bordered="false" :title="chartCardTitle" >
      <div slot="extra">
        <a-select defaultValue="7day" style="width: 110px" @change="charRetainChange">
          <a-select-opt-group>
            <span slot="label">留存</span>
            <a-select-option value="1day">1日留存</a-select-option>
            <a-select-option value="3day">3日留存</a-select-option>
            <a-select-option value="7day">7日留存</a-select-option>
            <a-select-option value="14day">14日留存</a-select-option>
            <a-select-option value="30day">30日留存</a-select-option>
          </a-select-opt-group>
          <a-select-opt-group label="流失">
            <a-select-option value="1dayLoss">1日流失</a-select-option>
            <a-select-option value="3dayLoss">3日流失</a-select-option>
            <a-select-option value="7dayLoss">7日流失</a-select-option>
            <a-select-option value="14dayLoss">14日流失</a-select-option>
            <a-select-option value="30dayLoss">30日流失</a-select-option>
          </a-select-opt-group>
        </a-select>
        <a-radio-group :value="chartRadioDefaultValue" buttonStyle="solid" :style="{marginLeft:'6.18px'}"
                       @change="chartShortcutDate" ref="chartRadioDate">
<!--          <a-radio-button value="1">今天</a-radio-button>-->
          <a-radio-button value="2">昨天</a-radio-button>
          <a-radio-button value="7">最近7天</a-radio-button>
          <a-radio-button value="30">最近30天</a-radio-button>
        </a-radio-group>
        <a-range-picker ref="chartDate"
                        :style="{width: '256px',marginLeft:'6.18px'}"
                        :defaultValue="chartDateVal"
                        :disabledDate="disabledDate"
                        @change="chartDateChange"/>
      </div>

      <a-row :gutter="0" :style="{minHeight:'390px'}">
        <g2-line ref="G2Line" :chartData="serverData" :fields="fields" :retains="retains" :title="title"></g2-line>
      </a-row>
    </a-card>

    <a-card :bordered="false" title="数据明细" :style="{ marginTop: '12px' }" :bodyStyle="{padding:'0px'}">
      <div slot="extra">
        <item-select ref="tableItemSelect" @change="itemSelectChange" :styleset="{width: '252px'}" :options="itemSelectData" />
        <a-select defaultValue="7day" style="width: 110px" @change="tableRetainChange">
          <a-select-opt-group>
            <span slot="label">留存</span>
            <a-select-option value="1day">1日留存</a-select-option>
            <a-select-option value="3day">3日留存</a-select-option>
            <a-select-option value="7day">7日留存</a-select-option>
            <a-select-option value="14day">14日留存</a-select-option>
            <a-select-option value="30day">30日留存</a-select-option>
          </a-select-opt-group>
          <a-select-opt-group label="流失">
            <a-select-option value="1dayLoss">1日流失</a-select-option>
            <a-select-option value="3dayLoss">3日流失</a-select-option>
            <a-select-option value="7dayLoss">7日流失</a-select-option>
            <a-select-option value="14dayLoss">14日流失</a-select-option>
            <a-select-option value="30dayLoss">30日流失</a-select-option>
          </a-select-opt-group>
        </a-select>
        <a-radio-group :value="tableRadioDefaultValue" buttonStyle="solid" @change="tableShortcutDate" ref="tableRadioDate" :style="{marginLeft:'6.18px'}">
<!--          <a-radio-button value="1">今天</a-radio-button>-->
          <a-radio-button value="2">昨天</a-radio-button>
          <a-radio-button value="7">最近7天</a-radio-button>
          <a-radio-button value="30">最近30天</a-radio-button>
        </a-radio-group>
        <a-range-picker ref="tableDate"
                        :style="{width: '256px',marginLeft:'6.18px'}"
                        :defaultValue="tableDateVal"
                        :disabledDate="disabledDate"
                        @change="tableDateChange"
                        slot="extra"/>
      </div>
      <a-table :dataSource="tableData" :columns="tableCol" :size="tableSize" :scroll="tableScroll"
               @change="tableChange">
        <div slot="depth" slot-scope="depth">
          <span style="text-decoration: underline;text-shadow: none;">{{depth.substring(0,depth.indexOf('(')).toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,')}}</span><br/>
          <span>{{depth.substring(depth.indexOf('(')+1,depth.length-1)}}</span>
        </div><br/>

      </a-table>
    </a-card>

  </div>
</template>

<script>
  import G2Line from '@/components/chart/Line2'
  import TargetSelect from '../components/TargetSelect'
  import ItemSelect from '../components/ItemSelect'
  import { postAction } from '@/api/manage'
  import { getAction } from '@/api/manage'
  import moment from "moment"

  export default {
    name: 'ArchivesStatisticst',
    components: {
      G2Line,
      TargetSelect,
      ItemSelect,
    },
    data() {
      return {
        title:"",
        maxTagCount:0,
        chartCardTitle:"图表分析",
        chartRadioDefaultValue:"7",
        tableRadioDefaultValue:"7",
        tableScroll:{},
        tableSize:"middle",
        chartDateVal:[moment().subtract('days', 7).startOf('day'), moment().subtract('days', 1).startOf('day')],
        tableDateVal:[moment().subtract('days', 7).startOf('day'), moment().subtract('days', 1).startOf('day')],
        // 数据集
        serverData: [],
        fields:[],
        retains:'actdt',
        // 字典 data
        itemSelectData:[
          {ifChecked: 1, label: "日期", value: "actdt"},
          {ifChecked: 0, label: "注册站点", value: "regsiteid"},
          {ifChecked: 0, label: "注册短渠道", value: "regchannelid"},
          {ifChecked: 0, label: "玩家vip等级", value: "viplevel"},
        ],
        url: {
          getChart: "/bigdata/kylin/retainLine",
          getTable: "/bigdata/kylin/retainTable"
        },
        tableCol:[],
        tableData:[],
        chartsParam:{
          dt:moment().subtract('days', 7).startOf('day').format("YYYY-MM-DD")+"~"+moment().subtract('days', 1).startOf('day').format("YYYY-MM-DD"),
          item: "actdt",
          targetName: "7day",
          xAxisName: 'dt'
        },
        tableParam:{
          dt:moment().subtract('days', 7).startOf('day').format("YYYY-MM-DD")+"~"+moment().subtract('days', 1).startOf('day').format("YYYY-MM-DD"),
          item: "actdt",
          targetName: '7day'
        },
      }
    },
    created () {

    },
    mounted() {
      this.$nextTick(() =>{
        this.$refs.tableItemSelect.setDefVal(['actdt']);
        this.loadChart();
        this.loadTable();
        this.watch();
      });
    },
    methods: {
      // Chart 相关 >>
      charRetainChange(v, o){
        this.chartsParam.targetName = v;
      }, // 留存/流失 切换
      chartDateChange(e, v) {
        this.chartRadioDefaultValue=false;
        this.chartsParam.dt= v[0]+"~"+v[1];
      }, // 日期筛选
      chartShortcutDate(e){
        this.chartRadioDefaultValue=e.target.value;
        if(e.target.value=='1'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 0).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
        }else if(e.target.value=='2'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 1).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 1).startOf('day'));
        }else if(e.target.value=='7'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 7).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 1).startOf('day'));
        }else if(e.target.value=='30'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 31).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 1).startOf('day'));
        }
        this.chartsParam.dt= this.chartDateVal[0].format("YYYY-MM-DD")+"~"+this.chartDateVal[1].format("YYYY-MM-DD")
      }, // 日期筛选（快捷切换）
      loadChart() {
        var retainType={};
        if(this.chartsParam.targetName.indexOf("Loss")>-1){
          retainType={
            retainType:'loss',
            targetName:this.chartsParam.targetName.replace("Loss","")
          }
        }
        postAction(this.url.getChart,Object.assign(this.chartsParam,retainType)).then((res) => {
          this.serverData = [];
          // this.serverData = [
          //   {'actdt':'2019-10-26','usernum':3236,'s1':2960,'s2':2862,'s3':2821,'s4':2812,'s5':2767},
          //   {'actdt':'2019-10-27','usernum':3177,'s1':2850,'s2':2811,'s3':2749,'s4':2737,'s5':null},
          //   {'actdt':'2019-10-28','usernum':3190,'s1':2899,'s2':2827,'s3':2792,'s4':null,'s5':null},
          //   {'actdt':'2019-10-29','usernum':2942,'s1':2682,'s2':2625,'s3':null,'s4':null,'s5':null},
          //   {'actdt':'2019-10-30','usernum':3039,'s1':2769,'s2':null,'s3':null,'s4':null,'s5':null},
          //   {'actdt':'2019-10-31','usernum':3078,'s1':null,'s2':null,'s3':null,'s4':null,'s5':null}
          // ]
          if (res.success) {
            if(Object.keys(res.result).length!=0){
              this.serverData=res.result.data;
              this.fields=res.result.fields;
            }else{
              this.$message.warning("没有查找到数据请尝试调整时间段或筛选条件");
            }
          }else{
            var that=this;
            that.$message.warning(res.message);
          }
        })
      }, // 获取Chart数据
      // Table 相关 >>
      tableRetainChange(v, o){
        this.tableParam.targetName = v;
      }, // 留存/流失 切换
      tableDateChange(e, v) {
        this.tableRadioDefaultValue=false;
        this.tableParam.dt= v[0]+"~"+v[1];
      }, // 日期筛选
      tableShortcutDate(e){
        this.tableRadioDefaultValue=e.target.value;
        if(e.target.value=='1'){
          this.tableDateVal.splice(0,1,moment().subtract('days', 0).startOf('day'));
          this.tableDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
        }else if(e.target.value=='2'){
          this.tableDateVal.splice(0,1,moment().subtract('days', 1).startOf('day'));
          this.tableDateVal.splice(1,1,moment().subtract('days', 1).startOf('day'));
        }else if(e.target.value=='7'){
          this.tableDateVal.splice(0,1,moment().subtract('days', 7).startOf('day'));
          this.tableDateVal.splice(1,1,moment().subtract('days', 1).startOf('day'));
        }else if(e.target.value=='30'){
          this.tableDateVal.splice(0,1,moment().subtract('days', 31).startOf('day'));
          this.tableDateVal.splice(1,1,moment().subtract('days', 1).startOf('day'));
        }
        this.tableParam.dt= this.tableDateVal[0].format("YYYY-MM-DD")+"~"+this.tableDateVal[1].format("YYYY-MM-DD")
      }, // 日期筛选（快捷切换）
      tableChange(pagination, filters, sorter) {
        // 排序
        if (Object.keys(sorter).length > 0) {
          this.tableParam['orderCol'] = sorter.field;
          this.tableParam['order'] = 'ascend' == sorter.order ? 'asc' : 'desc';
        }
        //this.loadTable();
      }, // Table排序
      loadTable(){
        var retainType={};
        if(this.tableParam.targetName.indexOf("Loss")>-1){
          retainType={
            retainType:'loss',
            targetName:this.tableParam.targetName.replace("Loss","")
          }
        }
        postAction(this.url.getTable,Object.assign({},this.tableParam,retainType)).then((res) => {
          this.title="捕鱼净分";
          if (res.success) {
            if(res.result.tableName.length>10){
              this.tableSize = "small";
            }else{
              this.tableSize = "middle";
            }
            const tableColAr=[]
            var tableWidth=0;
            var itemLength = this.tableParam.item.split(",").length;
            for(let i =0; i<res.result.tableName.length;i++){
              var col={
                title: res.result.tableTitle[i],
                dataIndex: res.result.tableName[i],
                key:res.result.tableName[i],
                // align:"right",
                sorter: true
              }
              if(i==0){
                //col['fixed']='left';  由于列是动态，如过列宽度少于页面显示宽度，不支持 fixed。暂不使用
              }
              col['width'] = parseInt("120");
              tableWidth+=parseInt("120");

              if( i > itemLength){
                col['scopedSlots']={
                  customRender:'depth',
                }
                col['customCell'] = function(record, rowIndex) {
                  var ratio = record[res.result.tableName[i]].substring(record[res.result.tableName[i]].indexOf("(")+1,record[res.result.tableName[i]].indexOf("%"));
                  var ratioClass;
                  if(ratio>90){
                    ratioClass="hm-9"
                  }else if(ratio>80){
                    ratioClass="hm-8"
                  }else if(ratio>70){
                    ratioClass="hm-7"
                  }else if(ratio>60){
                    ratioClass="hm-6"
                  }else if(ratio>50){
                    ratioClass="hm-5"
                  }else if(ratio>40){
                    ratioClass="hm-4"
                  }else if(ratio>30){
                    ratioClass="hm-3"
                  }else if(ratio>20){
                    ratioClass="hm-2"
                  }else if(ratio>10){
                    ratioClass="hm-1"
                  }else if(ratio>0){
                    ratioClass="hm-0"
                  }else{

                  }
                  return {class:ratioClass};
                }
              }
              // col['customRender']=function (t,r,index) {
              //   t = t.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,');
              //   if( i > targetLength+1){
              //     var ratio = r[res.result.tableName[i]].substring(r[res.result.tableName[i]].indexOf("(")+1,r[res.result.tableName[i]].indexOf("%"));
              //     var ratioClass;
              //     if(ratio>90){
              //       ratioClass="hm-1"
              //     }else if(ratio>80){
              //       ratioClass="hm-2"
              //     }else if(ratio>70){
              //       ratioClass="hm-3"
              //     }else if(ratio>60){
              //       ratioClass="hm-4"
              //     }else if(ratio>50){
              //       ratioClass="hm-5"
              //     }else if(ratio>40){
              //       ratioClass="hm-6"
              //     }else if(ratio>30){
              //       ratioClass="hm-7"
              //     }else if(ratio>20){
              //       ratioClass="hm-8"
              //     }else if(ratio>10){
              //       ratioClass="hm-9"
              //     }else{
              //       ratioClass="hm-10"
              //     }
              //     t = "<span class='"+ratioClass+"'>"+t+"</span>";
              //   }
              //   return t;
              // }
              tableColAr.push(col);
              this.tableScroll = {x:tableWidth};
            }
            this.tableCol=tableColAr;
            this.tableData = res.result.tableData;
          }else{
            var that=this;
            that.$message.warning(res.message);
          }
        })
      },  // 获取Table数据
      numberClass(ratio){

      }, // 留存数值对应 深浅
      // 通用 >>
      disabledDate(current){
        return current && current > moment().endOf('day');
      }, // 最大日期
      targetSelectChange(selectName,v) {
        if(selectName == "chart"){
          this.chartsParam.targetName = v;
          //this.loadChart();
        }else if(selectName == "table"){
          this.tableParam.targetName = v;
          //this.loadTable();
        }
      }, // 指标选中回调
      itemSelectChange(v){
        this.tableParam.item = v.join(",");
        //this.loadTable();
      }, // 维度选中回调
      getEventItem:function () {
        getAction(this.url.getItem,{eventId:this.eventId}).then((res)=>{
          if(res.success){
            var defaultValue=[];
            for (let i = 0; i < res.result.length ; i++) {
              if(res.result[i].ifChecked=='1'){
                defaultValue.push(res.result[i].value)
              }
            }
            if(res.result.length>0){
              this.itemSelectData=res.result;
              this.tableParam.item = defaultValue.join(",");
              this.$refs.tableItemSelect.setDefVal(defaultValue);
            }else{
              this.$message.warning("未配置维度，请联系管理员！");
            }
          }
        })
      }, // 获取维度
      watch(){
        this.$watch('chartsParam',
          function(){
            if(
              this.chartsParam.dt!=""
              && this.chartsParam.targetName.length > 0
              && this.chartsParam.item.length > 0
            ){
              this.loadChart();
            }
          }, {deep: true});
        this.$watch('tableParam',
          function(n,o){
            if(
              this.tableParam.dt!=""
              && this.tableParam.targetName.length > 0
              && this.tableParam.item.length > 0
            ){
              this.loadTable();
            }
          }, {deep: true});
      }, // 监控 图、报表条件 变化
    }
  }
</script>
<style scoped>
  .ant-card-body .table-operator {
    margin-bottom: 18px;
  }

  .ant-table-tbody .ant-table-row td {
    padding-top: 15px;
    padding-bottom: 15px;
  }

  .anty-row-operator button {
    margin: 0 5px
  }

  .ant-btn-danger {
    background-color: #ffffff
  }

  .ant-modal-cust-warp {
    height: 100%
  }

  .ant-modal-cust-warp .ant-modal-body {
    height: calc(100% - 110px) !important;
    overflow-y: auto
  }

  .ant-modal-cust-warp .ant-modal-content {
    height: 90% !important;
    overflow-y: hidden
  }

  .statistic {
    padding: 0px !important;
    margin-top: 50px;
  }

  .statistic h4 {
    margin-bottom: 20px;
    text-align: center !important;
    font-size: 24px !important;;
  }

  .statistic #canvas_1 {
    width: 100% !important;
  }
</style>

<style>
  .ant-table-fixed tr td:hover{
    pointer-events: none;
  }
  .ant-table-fixed tr td{
    padding: 5px 15px !important;
    height: 53px !important;
  }
  .hm-9{
    background-color:#135943!important;
    color:#fff;
    border-width: 0px 0 0px!important;
    text-shadow:0 0 5px rgba(0,0,0,.6);
  }
  .hm-9:hover {
    background-color:#1a7b5c;
  }
  .hm-8 {
    background-color:#187255;
    color:#fff;
    border-width: 0px 0 0px!important;
    text-shadow:0 0 5px rgba(0,0,0,.6);
  }
  .hm-8:hover {
    background-color:#1f946f;
  }
  .hm-7 {
    background-color:#1d8c68;
    color:#fff;
    border-width: 0px 0 0px!important;
    text-shadow:0 0 5px rgba(0,0,0,.6);
  }
  .hm-7:hover {
    background-color:#25ad82;
  }
  .hm-6 {
    background-color:#23a57b;
    color:#fff;
    border-width: 0px 0 0px!important;
    text-shadow:0 0 5px rgba(0,0,0,.6);
  }
  .hm-6:hover {
    background-color:#2ac794;
  }
  .hm-5 {
    background-color:#28be8e;
    color:#fff;
    border-width: 0px 0 0px!important;
    text-shadow:0 0 5px rgba(0,0,0,.6);
  }
  .hm-5:hover {
    background-color:#3ad5a4;
  }
  .hm-4 {
    background-color:#31d4a0;
    color:#4C535A;
    border-width: 0px 0 0px!important;
    text-shadow:none;
  }
  .hm-4:hover {
    background-color:#53dbaf;
  }
  .hm-3 {
    background-color:#4ad9ab;
    color:#4C535A;
    border-width: 0px 0 0px!important;
    text-shadow:none;
  }
  .hm-3:hover {
    background-color:#6ce0bb;
  }
  .hm-2 {
    background-color:#64deb7;
    color:#4C535A;
    border-width: 0px 0 0px!important;
    text-shadow:none;
  }
  .hm-2:hover {
    background-color:#85e5c7;
  }
  .hm-1 {
    background-color:#7de4c3;
    color:#4C535A;
    border-width: 0px 0 0px!important;
    text-shadow:none;
  }
  .hm-1:hover {
    background-color:#9febd2;
  }
  .hm-0 {
    background-color:#96e9ce;
    color:#4C535A;
    border-width: 0px 0 0px!important;
    text-shadow:none;
  }
  .hm-0:hover {
    background-color:#b8f0de;
  }
</style>