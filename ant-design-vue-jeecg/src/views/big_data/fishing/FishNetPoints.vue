<template>
  <a-card :bordered="false" title="捕鱼净分分析" :style="{ marginTop: '24px' }">

    <div slot="extra">
      <a-radio-group defaultValue="7" buttonStyle="solid" @change="changeFDate">
        <a-radio-button value="1">今天</a-radio-button>
        <a-radio-button value="2">昨天</a-radio-button>
        <a-radio-button value="7">最近7天</a-radio-button>
        <a-radio-button value="30">最近30天</a-radio-button>
      </a-radio-group>
      <a-range-picker
        :style="{width: '256px',marginLeft:'24px'}"
        :defaultValue="chartDateVal"
        @change="handleChartsChange"
                      slot="extra"/>
    </div>

    <a-row :gutter="24">
      <a-col :span="10">
        <a-select defaultValue="日" style="width: 75px">
          <a-select-option value="年">按年</a-select-option>
          <a-select-option value="月">按月</a-select-option>
          <a-select-option value="日">按日</a-select-option>
          <a-select-option value="时">按时</a-select-option>
        </a-select>
      </a-col>
      <g2-line :chartData="serverData" ref="G2Line" :title="title"></g2-line>
    </a-row>
  </a-card>
</template>

<script>
  import G2Line from '@/components/chart/Line'
  import { postAction } from '@/api/manage'
  import moment from "moment"

  export default {
    name: 'ArchivesStatisticst',
    components: {
      G2Line
    },
    data() {
      return {
        chartDateVal:[moment().subtract('days', 6).startOf('day'), moment().subtract('days', 0).startOf('day')],
        // 数据集
        serverData: [
        ],
        title:'',
        url: {
          getChart: "/bigdata/kylin/line"
        },
        chartsParam:{
          dt:"2019-10-12~2019-10-18",
          eventId: "1",
          targetName: [
            "fishpuregamecoin", "gametax", "fishcoincost", "gamecoingive", "redpacketgive", "bulletprovide",
            "keyprovide","inkindlotteryprovide","squarepuregamecoin","jellyfishpuregamecoin","bulletdiffvalue",
            "jellyfishdiffvalue","leishirest"
          ],
          xAxisName: "timeyear,timemonth,timeday"
        }
      }
    },
    created() {
      let url = this.url.getChart;
      this.loadChart(url,this.chartsParam);
    },
    methods: {
      // 日期范围发生变化的回调
      handleChartsChange(e, v) {
        this.chartsParam['dt']=v[0]+"~"+v[1];
        let url = this.url.getChart;
        this.loadChart(url,this.chartsParam);
      },
      changeFDate(e){
        if(e.target.value=='1'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 0).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
          this.chartsParam['dt']=moment().subtract('days', 0).startOf('day').format('YYYY-MM-DD')+"~"+moment().subtract('days', 0).startOf('day').format('YYYY-MM-DD');;
        }else if(e.target.value=='2'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 1).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 1).startOf('day'));
          this.chartsParam['dt']=moment().subtract('days', 1).startOf('day').format('YYYY-MM-DD')+"~"+moment().subtract('days', 1).startOf('day').format('YYYY-MM-DD');;
        }else if(e.target.value=='7'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 6).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
          this.chartsParam['dt']=moment().subtract('days', 6).startOf('day').format('YYYY-MM-DD')+"~"+moment().subtract('days', 0).startOf('day').format('YYYY-MM-DD');;
        }else if(e.target.value=='30'){
          this.chartDateVal.splice(0,1,moment().subtract('days', 30).startOf('day'));
          this.chartDateVal.splice(1,1,moment().subtract('days', 0).startOf('day'));
          this.chartsParam['dt']=moment().subtract('days', 30).startOf('day').format('YYYY-MM-DD')+"~"+moment().subtract('days', 0).startOf('day').format('YYYY-MM-DD');;
        }
        this.loadChart(this.url.getChart,this.chartsParam)
      },
      loadChart(url,param) {
        postAction(url,param).then((res) => {
          this.title="捕鱼净分";
          this.serverData = [];
          if (res.success) {
            this.serverData=res.result.data;
          }else{
            var that=this;
            that.$message.warning(res.message);
          }
        })
      },
      moment,
    },
    watch:{

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