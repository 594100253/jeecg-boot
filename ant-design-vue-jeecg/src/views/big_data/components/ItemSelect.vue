<template>
  <a-select placeholder="请选择维度"
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
    },
    // 方法集合
    methods: {
      change(value, option){
        if(value.length>0){
          // this.defaultValue = value;
          this.$emit('change', value)
        }else {
          this.$message.warning("请选择至少一条维度！");
        }
      }, // 选中事件
      setDefVal(v){
        this.selectedValue = v;
      }
    },
  }
</script>