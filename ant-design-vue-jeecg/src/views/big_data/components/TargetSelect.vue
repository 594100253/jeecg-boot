<template>
  <a-select placeholder="请选择指标"
            :maxTagCount="2" showArrow :maxTagTextLength="2"
            mode="multiple" allowClear
            :style="styleset" v-model="selectedValue"
            :options="options" @change="change"
  >
  </a-select>
</template>

<script>
  export default {
    created(){
    },
    props:{
      selectName:{
        type: String
      },
      options:{
        type:Array,
        required: false,
      },
      styleset:{
        type:Object,
        required: false,
      }
    },
    data(){
      return {
        selectedValue:[],
      }
    },
    mounted(){
      this.getEventTarget()
    },
    // 方法集合
    methods: {
      getEventTarget:function () {
      },
      change(value, option){
        if(value.length>0){
          // this.selectedValue = v;
          this.$emit('change', this.selectName,value)
        }else {
          this.$message.warning("请选择至少一条指标！");
        }
      }, // 选中事件
      setDefVal(v){
        this.selectedValue = v;
      }, // set 默认选项
    },
  }
</script>