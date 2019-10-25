<template>
  <div>
    <a-card :bordered="false" :title="chartCardTitle" >
      <div slot="extra">
        <target-select selectName="chart" @change="targetSelectChange" />
        <a-select defaultValue="timeyear,timemonth,timeday"
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
        <g2-line :chartData="serverData" ref="G2Line" :title="title"></g2-line>
      </a-row>
    </a-card>

    <a-card :bordered="false" title="数据明细" :style="{ marginTop: '12px' }" :bodyStyle="{padding:'0px'}">
      <div slot="extra">
        <item-select @change="itemSelectChange" />
        <target-select selectName="table" @change="targetSelectChange" />
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
  import G2Line from '@/components/chart/Line'
  import TargetSelect from '../components/TargetSelect'
  import ItemSelect from '../components/ItemSelect'
  import { postAction } from '@/api/manage'
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
        title:'',
        url: {
          getChart: "/bigdata/kylin/line",
          getTable: "/bigdata/kylin/table"
        },
        chartsParam:{
          eventId: "1",
          targetName: [],
          xAxisName: 'timeyear,timemonth,timeday'
        },
        tableParam:{
          eventId: "1",
          targetName: [],
          itemName: []
        },
        tableCol:[],
        tableData:[]
      }
    },
    watch:{
      'chartsParam':{
        handler(newVal, oldVal) {
          if(
            this.chartsParam.dt!=""
            && this.chartsParam.targetName.length > 0
            && this.chartsParam.xAxisName.length > 0
          ){
            this.loadChart();
          }
        },
        deep: true
      },
      'tableParam':{
        handler(newVal, oldVal) {
          if(
            this.tableParam.dt!=""
            && this.tableParam.targetName.length > 0
            && this.tableParam.itemName.length > 0
          ){
            this.loadTable();
          }
        },
        deep: true
      }
    },
    mounted() {
      this.$nextTick(() =>{
      });
    },
    methods: {
      // Chart 相关 >>
      chartDateChange(e, v) {
        this.chartRadioDefaultValue=false;
        this.chartDateVal.splice(0,1,e[0]);
        this.chartDateVal.splice(1,1,e[1]);
        this.loadChart();
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
        this.loadChart()
      }, // 日期筛选（快捷切换）
      chartXChange(v){
        this.chartsParam.xAxisName = v;
      }, // 切换 X轴
      loadChart() {
        // var begin =this.$refs.chartDate.$options._parentVnode.elm.childNodes[0].children[0].value;
        // var end =this.$refs.chartDate.$options._parentVnode.elm.childNodes[0].children[2].value;
        this.chartsParam['dt']=this.chartDateVal[0].format("YYYY-MM-DD")+"~"+this.chartDateVal[1].format("YYYY-MM-DD");
        postAction(this.url.getChart,this.chartsParam).then((res) => {
          this.serverData = [];
          if (res.success) {
            this.serverData=res.result.data;
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
        this.tableDateVal.splice(0,1,e[0]);
        this.tableDateVal.splice(1,1,e[1]);
        //this.loadTable();
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
        //this.loadTable();
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
        this.tableParam['dt']=this.tableDateVal[0].format("YYYY-MM-DD")+"~"+this.tableDateVal[1].format("YYYY-MM-DD");
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
              col['width'] = parseInt(res.result.tableNameWidth[i]);
              if(i<res.result.tableName.length-1){
              }
              tableWidth+=parseInt(res.result.tableNameWidth[i]);
              col['customRender']=function (t,r,index) {
                return t.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,');
              }
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
      },
      targetSelectChange(selectName,v) {
       if(selectName == "chart"){
         this.chartsParam.targetName = v;
         //this.loadChart();
       }else if(selectName == "table"){
         this.tableParam.targetName = v;
         //this.loadTable();
       }
      },
      itemSelectChange(v){
        this.tableParam.itemName = v;
        //this.loadTable();
      }
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