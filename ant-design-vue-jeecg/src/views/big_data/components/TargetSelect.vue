<template>
  <a-select placeholder="请选择指标"
            :maxTagCount="2" showArrow :maxTagTextLength="2"
            mode="multiple" allowClear
            :style="style" v-model="defaultValue"
            :options="data" @change="change"
  >
  </a-select>
</template>

<script>
  import { getAction } from '@/api/manage'

  export default {
    created(){
    },
    props:{
      selectName:{
        type: String
      }
    },
    data(){
      return {
        style: { width: '252px' },
        data:[],
        defaultValue:[],
        url:{
          getTarget:"/bigdata/targetDic/getEventTargets",
        }
      }
    },
    mounted(){
      this.getEventTarget()
    },
    // 方法集合
    methods: {
      getEventTarget:function () {
        getAction(this.url.getTarget,{eventId:1}).then((res)=>{
          if(res.success){
            var defaultValue=[];
            for (let i = 0; i < res.result.length ; i++) {
              if(res.result[i].ifChecked=='1'){
                defaultValue.push(res.result[i].value)
              }
            }
            this.defaultValue = defaultValue;
            this.data=res.result;
            if(this.defaultValue.length>0){
              this.$emit('change', this.selectName,this.defaultValue)
            }else{
              this.$message.warning("未配置指标，请联系管理员！");
            }
          }
        })
      },
      change(value, option){
        if(value.length>0){
          this.$emit('change', this.selectName,value)
        }else {
          this.$message.warning("请选择至少一条指标！");
        }
      },
    },
  }
</script>