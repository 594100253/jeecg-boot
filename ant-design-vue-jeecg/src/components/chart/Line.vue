<template>
  <div>
    <p>{{title}}</p>
    <div id="c1"></div>
  </div>
</template>

<script>
  // 这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
  // 例如：import 《组件名称》 from '《组件路径》';
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
      init() {
        // 此函数为个人习惯,喜欢创建一个初始化的函数
        this.chart = new G2.Chart({
          container: "c1",
          forceFit:true,
          width: this.width,
          padding:'auto',
          height: this.height
        })
        this.chart.source(this.chartData);
        this.chart.axis('TARGET', {
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
        this.chart.line().position('DATE*TARGET').color('TARGETTYPE').shape('smooth');
        this.chart.point().position('DATE*TARGET').color('TARGETTYPE').size(4).shape('circle').style({
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
              this.chart.changeData(newVal);
            } else {
              this.chart.changeData(oldVal);
            }
          } else {
            this.init();
          }

        }
      }
    }
  }
</script>