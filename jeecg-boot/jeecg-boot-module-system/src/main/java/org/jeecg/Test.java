package org.jeecg;


import com.alibaba.fastjson.JSONObject;

public class Test {
	public static void main(String[] args) {

		String a = "usernum as '玩家总数',decode(sign(s1),0,null,1,s1) as '第1天'";

		System.out.println(a.replaceAll("decode[(]sign[(]s","decode(sign(usernum-s"));
	}
}
