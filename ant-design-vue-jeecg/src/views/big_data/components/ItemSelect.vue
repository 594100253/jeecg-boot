<template>
  <a-select placeholder="请选择维度"
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
    },
    data(){
      return {
        style: { width: '252px' },
        data:[],
        defaultValue:[],
        url:{
          getItem:"/bigdata/itemDic/getEventItems",
        }
      }
    },
    mounted(){
      this.getEventItem()
    },
    // 方法集合
    methods: {
      getEventItem:function () {
        getAction(this.url.getItem,{eventId:1}).then((res)=>{
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
              this.$emit('change',this.defaultValue)
            }else{
              this.$message.warning("未配置维度，请联系管理员！");
            }
          }
        })
      },
      change(value, option){
        if(value.length>0){
          this.$emit('change', value)
        }else {
          this.$message.warning("请选择至少一条维度！");
        }
      },
    },
  }
</script>