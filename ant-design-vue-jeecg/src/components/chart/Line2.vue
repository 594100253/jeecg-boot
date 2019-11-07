<template>
  <div>
    <p>{{title}}</p>
    <div id="c1"></div>
  </div>
</template>

<script>
  import { DataSet } from '@antv/data-set'

  export default {
    props:{
      title:{
        type: String,
        default: 'y'
      },
      chartData: {
        type: Array,
        required: true
      },
      fields: {
        type: Array,
        required: true
      },
      retains: {
        type: String,
        required: true
      },
      width: {
        type: Number,
        default: 900
      },
      height: {
        type: Number,
        default: 350
      }
    },
    data(){
      return {
        scale: [{
          dataKey: 'x',
          min: 0,
          max: 1
        }],
        style: { stroke: '#fff', lineWidth: 1 }
      }
    },
    // 方法集合
    methods: {
      data(){
        var ds = new DataSet();
        const dv = ds.createView()
          .source(this.chartData)
          .transform({
            type: 'fold',
            fields: this.fields, // 展开字段集
            key: 'key',                   // key字段
            value: 'value',               // value字段
            retains: [ this.retains ]        // 保留字段集，默认为除 fields 以外的所有字段
          });
        return dv.rows;
      },
      init() {
        this.chart = new G2.Chart({
          container: "c1",
          forceFit:true,
          width: this.width,
          padding:'auto',
          height: this.height
        })
        this.chart.source(this.data());
        this.chart.axis('value', {
          label: {
            textStyle: {
              fill: '#aaaaaa'
            },
            formatter: function formatter(num) {
              // return text.replace(/(\d)(?=(?:\d{3})+$)/g, '$1,');
              var v = num;
              var numericSymbols = ['亿亿','万亿','亿','万'];
              var numericSymbolMagnitude = ['10000000000000000','1000000000000','100000000','10000']
              for (let i = 0; i < numericSymbols.length ; i++) {
                if( num/numericSymbolMagnitude[i] !=0 && num%numericSymbolMagnitude[i] == 0 ){
                  v = num.substring(0,num.length-(numericSymbolMagnitude[i].length-1)) + numericSymbols[i];
                  break;
                }
              }
              return v;
            }
          }
        });
        this.chart.tooltip({
          crosshairs: {
            type: 'line'
          }
        });
        this.chart.line().position(this.retains+'*value').color('key').shape('smooth');
        this.chart.point().position(this.retains+'*value').color('key').size(4).shape('circle').style({
          stroke: '#fff',
          lineWidth: 1
        });
        this.chart.render();
      },
      changeData(data){
        this.chart.changeData(this.chartData);
        this.chart.source(data);

        this.chart.guide().clear();// 清理guide
        this.chart.repaint();
      }
    },
    // 监控 数据变化
    watch:{
      chartData:{
        handler(newVal, oldVal) {
          if(this.chart){
            if (newVal) {
              var data = this.data();
              this.chart.changeData(data);
            }
          } else {
            this.init();
          }
        }
      }
    }
  }
</script>