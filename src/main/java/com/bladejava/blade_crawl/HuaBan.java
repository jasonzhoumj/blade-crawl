package com.bladejava.blade_crawl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blade.kit.FileKit;
import blade.kit.StringKit;
import blade.kit.http.HttpRequest;
import blade.kit.json.JSONKit;
import blade.kit.json.JSONObject;
import blade.kit.log.Logger;

/**
 * 抓取花瓣网的图片例子
 * @version 
 * @since JDK 1.8
 */
public class HuaBan {

	private static final Logger LOGGER = Logger.getLogger(HuaBan.class);
	
	private List<Map<String, Object>> images;
	
	private static final String homeUrl = "http://huaban.com/favorite/photography";
	
	public HuaBan() {
		if(!FileKit.isDirectory("images")){
			FileKit.createDir("images");
		}
		images = new ArrayList<Map<String,Object>>();
	}
	
	/**
	 * 加载页面
	 */
	public String loadHomePage(){
		return HttpRequest.get(homeUrl).body();
	}
	
	/**
	 * ajax链接
	 */
	public String makeAjaxUrl(int no){
		 //返回ajax请求的url
        return homeUrl + "?i5p998kw&max=" + no + "&limit=20&wfl=1";
	}
	
	/**
	 * 下拉刷新，加载更多
	 */
	public String loadMore(int maxNo){
		//返回ajax请求的url
		return HttpRequest.get(this.makeAjaxUrl(maxNo)).body();
	}
	
	/**
	 * 处理抓取到的页面
	 */
	public void processData(String htmlContent){
		//""" 从html页面中提取图片的信息 """
		int pos = htmlContent.indexOf("app.page[\"pins\"]");
		int start = 0, end = 0;
		if(pos != -1){
			start = pos + 19;
			end = htmlContent.indexOf("];", start) + 1;
			
			String result = htmlContent.substring(start, end);
			List<Map<String, Object>> listData = JSONKit.toList(result);
			
			for(Map<String, Object> map : listData){
				Map<String, Object> temp = new HashMap<String, Object>();
				temp.put("id", Integer.valueOf(map.get("pin_id").toString()));
				
				JSONObject file = (JSONObject) map.get("file");
				temp.put("url", "http://img.hb.aicdn.com/" + file.get("key") + "_fw658");
				String type = file.getString("type");
				if(StringKit.isNotBlank(type) && type.indexOf("image") != -1){
					temp.put("type", type.substring(type.indexOf("image") + 6));
				}
				images.add(temp);
			}
			
		}
		
	}
	
	/**
	 * 抓取图片主逻辑
	 */
	public HuaBan getImageInfo(int num){
		String homeContent = loadHomePage();
		processData(homeContent);
		
		for(int i=0; i<(num-1)/20; i++){
			Integer lastId = (Integer) images.get(images.size() - 1).get("id");
			processData(loadMore(lastId));
		}
		
		return this;
	}
	
	/**
	 * 下载图片
	 */
	public void download(){
		if(images.size() > 0){
			int count = 1;
			for(Map<String, Object> map : images){
				
				String fileName = map.get("id") + "." + map.get("type");
				
				File output = new File("images/" + fileName);
				
				HttpRequest.get(map.get("url").toString()).receive(output);
				LOGGER.info(count + " 下载完成：" + fileName);
				count++;
			}
		}
	}
	
	public static void main(String[] args) {
		// 抓取200张图片并下载
		new HuaBan().getImageInfo(200).download();
	}
}
