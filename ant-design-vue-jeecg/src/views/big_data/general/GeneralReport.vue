<template>
  <div>
    <a-card :bordered="false" :title="chartCardTitle" >
      <div slot="extra">
        <target-select ref="chartTargetSelect" @change="targetSelectChange" :eventId="eventId" :styleset="{width: '252px'}" selectName="chart" :options="targetSelectData" />
        <a-select defaultValue="timeyear,timemonth,timeday" v-if="ifGroupDate"
              @change="chartXChange"
              :style="{width: '75px',marginLeft:'6.18px'}">
          <a-select-option value="timeyear">按年</a-select-option>
          <a-select-option value="timeyear,timemonth">按月</a-select-option>
          <a-select-option value="timeyear,timemonth,timeday">按日</a-select-option>
          <a-select-option value="timeyear,timemonth,timeday,timehour">按时</a-select-option>
        </a-select>
        <a-radio-group :value="chartRadioDefaultValue" buttonStyle="solid" :style="{marginLeft:'6.18px'}"
                       @change="chartShortcutDate" ref="chartRadioDate">
          <a-radio-button value="1">今天</a-radio-button>
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
        <g2-line :chartData="serverData" ref="G2Line" :fields="fields" :retains="retains" :title="title"></g2-line>
      </a-row>
    </a-card>

    <a-card :bordered="false" title="数据明细" :style="{ marginTop: '12px' }" :bodyStyle="{padding:'0px'}">
      <div slot="extra">
        <item-select ref="tableItemSelect" @change="itemSelectChange" :eventId="eventId" :styleset="{width: '252px'}" :options="itemSelectData" />
        <target-select ref="tableTargetSelect" @change="targetSelectChange" :eventId="eventId" :styleset="{width: '252px'}" selectName="table" :options="targetSelectData" />
        <a-radio-group :value="tableRadioDefaultValue" buttonStyle="solid" @change="tableShortcutDate" ref="tableRadioDate" :style="{marginLeft:'6.18px'}">
          <a-radio-button value="1">今天</a-radio-button>
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
      <a-table
        :columns="tableCol"
        :dataSource="tableData"
        :size="tableSize"
        :scroll="tableScroll" @change="tableChange"
      />
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
        ifGroupDate:true,
        maxTagPlaceholder:"指标",
        maxTagCount:0,
        chartCardTitle:"图表分析",
        chartRadioDefaultValue:"7",
        tableRadioDefaultValue:"7",
        tableScroll:{},
        tableSize:"middle",
        chartDateVal:[moment().subtract('days', 6).startOf('day'), moment().subtract('days', 0).startOf('day')],
        tableDateVal:[moment().subtract('days', 6).startOf('day'), moment().subtract('days', 0).startOf('day')],
        // 数据集
        serverData: [
        ],
        fields:[],
        retains:'x',
        // 字典 data
        itemSelectData:[],
        targetSelectData:[],
        eventId:"",
        title:'',
        url: {
          getChart: "/bigdata/report/line",
          getTable: "/bigdata/report/table",
          getItem:"/bigdata/itemDic/getEventItemIds",
          getTarget:"/bigdata/targetDic/getEventTargetIds",
        },
        chartsParam:{
          dt:moment().subtract('days', 6).startOf('day').format("YYYY-MM-DD")+"~"+moment().subtract('days', 0).startOf('day').format("YYYY-MM-DD"),
          eventId: "1",
          target: [],
          xName: 'timeyear,timemonth,timeday'
        },
        tableParam:{
          dt:moment().subtract('days', 6).startOf('day').format("YYYY-MM-DD")+"~"+moment().subtract('days', 0).startOf('day').format("YYYY-MM-DD"),
          eventId: "1",
          target: [],
          item: []
        },
        tableCol:[],
        tableData:[]
      }
    },
    created () {
      // 获取自定义参数
      var url = this.$route.path;
      var paramsStr=url.substring(url.indexOf("^")+1);
      var paramsAr = paramsStr.split("&");
      var params ={};
      for (let i = 0; i < paramsAr.length ; i++) {
        var key = paramsAr[i].substring(0,paramsAr[i].indexOf("="));
        var val = paramsAr[i].substring(paramsAr[i].indexOf("=")+1);
        params[key] = val;
      }
      this.eventId = params.eventId;
      this.chartsParam['eventId']=this.eventId;
      this.tableParam['eventId']=this.eventId;

      // 数据问题，临时处理下
      if(params.eventId==80 || params.eventId==81){
        this.chartsParam.xName="DT";
        this.ifGroupDate=false;
      }

    },
    mounted() {
      this.$nextTick(() =>{
        // 获取 维度配置
        this.getEventItem();
        // 获取 指标配置
        this.getEventTarget();
        this.watch();
      });
    },
    methods: {
      // Chart 相关 >>
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
          this.chartDateVal.splice(0,1,moment().subtract('days', 6).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
        }else if(e.target.value=='30'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 30).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
        }
        this.chartsParam.dt= this.chartDateVal[0].format("YYYY-MM-DD")+"~"+this.chartDateVal[1].format("YYYY-MM-DD")
      }, // 日期筛选（快捷切换）
      chartXChange(v){
        this.chartsParam.xName = v;
      }, // 切换 X轴
      loadChart() {
        postAction(this.url.getChart,this.chartsParam).then((res) => {
          this.serverData = [];
          if (res.success) {
            if(Object.keys(res.result).length!=0){
              this.serverData=res.result.data;
              this.fields = res.result.fields;
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
      //table tableShortcutDate tableDateChange
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
          this.tableDateVal.splice(0,1,moment().subtract('days', 6).startOf('day'));
          this.tableDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
        }else if(e.target.value=='30'){
          this.tableDateVal.splice(0,1,moment().subtract('days', 30).startOf('day'));
          this.tableDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
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
        postAction(this.url.getTable,this.tableParam).then((res) => {
          this.title="捕鱼净分";
          if (res.success) {
            if(res.result.tableName.length>10){
              this.tableSize = "small";
            }else{
              this.tableSize = "middle";
            }
            const tableColAr=[]
            var tableWidth=0;
            for(let i =0; i<res.result.tableName.length;i++){
              var col={
                title: res.result.tableTitle[i],
                dataIndex: res.result.tableName[i],
                key:res.result.tableName[i],
                align:"right",
                sorter: true
              }
              if(i==0){
                //col['fixed']='left';  由于列是动态，如过列宽度少于页面显示宽度，不支持 fixed。暂不使用
              }
              col['width'] = parseInt(res.result.widths[i]);
              if(i<res.result.tableName.length-1){
              }
              tableWidth+=parseInt(res.result.widths[i]);
              // col['customRender']=function (t,r,index) {
              //   return t.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,');
              // }
              tableColAr.push(col);
              this.tableScroll = {x:tableWidth};
            }
            this.tableCol=tableColAr;
            this.tableData = res.result.records;
          }else{
            var that=this;
            that.$message.warning(res.message);
          }
        })
      },  // 获取Table数据
      // 通用 >>
      disabledDate(current){
        return current && current > moment().endOf('day');
      }, // 最大日期
      targetSelectChange(selectName,v) {
       if(selectName == "chart"){
         this.chartsParam.target = v;
         //this.loadChart();
       }else if(selectName == "table"){
         this.tableParam.target = v;
         //this.loadTable();
       }
        console.log(selectName,v)
        console.log(this.chartsParam)
      }, // 指标选中回调
      itemSelectChange(v){
        this.tableParam.item = v;
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
              this.tableParam.item = defaultValue;
              this.$refs.tableItemSelect.setDefVal(defaultValue);
            }else{
              this.$message.warning("未配置维度，请联系管理员！");
            }
          }
        })
      }, // 获取维度
      getEventTarget:function () {
        getAction(this.url.getTarget,{eventId:this.eventId}).then((res)=>{
          if(res.success){
            var defaultValue=[];
            for (let i = 0; i < res.result.length ; i++) {
              if(res.result[i].ifChecked=='1'){
                defaultValue.push(res.result[i].value)
              }
            }
            this.$refs.tableTargetSelect.setDefVal(defaultValue);
            this.$refs.chartTargetSelect.setDefVal(defaultValue);
            if(res.result.length>0){
              this.targetSelectData=res.result;
              this.tableParam.target = defaultValue;
              this.chartsParam.target = defaultValue;
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
              && this.chartsParam.target.length > 0
              && this.chartsParam.xName.length > 0
            ){
              this.loadChart();
            }
          }, {deep: true});
        this.$watch('tableParam',
          function(){
            if(
              this.tableParam.dt!=""
              && this.tableParam.target.length > 0
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