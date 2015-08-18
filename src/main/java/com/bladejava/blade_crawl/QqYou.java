package com.bladejava.blade_crawl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blade.kit.FileKit;
import blade.kit.http.HttpRequest;
import blade.kit.log.Logger;

/**
 * 抓取花瓣网的图片例子
 * @version 
 * @since JDK 1.8
 */
public class QqYou {

	private static final Logger LOGGER = Logger.getLogger(QqYou.class);
	
	private List<Map<String, Object>> images;
	
	private static final String site = "http://www.qqyou.com";
	
	private static final String homeUrl = site + "/touxiang/nvsheng/";
	
	public QqYou() {
		if(!FileKit.isDirectory("avatar")){
			FileKit.createDir("avatar");
		}
		images = new ArrayList<Map<String,Object>>();
	}
	
	/**
	 * 加载页面
	 */
	public String loadPage(int page){
		return HttpRequest.get(homeUrl + "list"+ page +".html").acceptCharset("gb2312").body();
	}
	
	/**
	 * 加载页面
	 */
	public List<String> loadItems(String html){
		List<String> items = new ArrayList<String>();
		Pattern pattern = Pattern.compile("<div.*?pics\">([\\s\\S]*?)<a href=\"([\\s\\S]*?)\".*?>([\\s\\S]*?)</a>([\\s\\S]*?)</div>");
		Matcher m = pattern.matcher(html);
		while(m.find()){
			String detailHref = site + m.group(2);
			items.add(detailHref);
		}
		return items;
	}
	
	/**
	 * 抓取图片主逻辑
	 */
	public void start(int page){
		start(1, page);
	}
	
	public QqYou start(int start_page, int end_page){
		if (start_page < 1) {
			start_page = 1;
		}
		
		if (end_page < 1) {
			end_page = 1;
		}
		
		for(int page=start_page; page <= end_page; page++){
			// 加载一页
			String content = loadPage(page);
			// 一页中的所有项
			List<String> items = loadItems(content);
			process(items);
		}
		return this;
	}
	
	public void process(List<String> items){
		Pattern pattern = Pattern.compile("<a.*?<img.*?class=img200X200.*?src=\"([\\s\\S]*?)\".*?</a>");
		
		for(String item : items){
			
			List<String> images = new ArrayList<String>();
			String content = HttpRequest.get(item).body();
			
			Matcher m = pattern.matcher(content);
			while(m.find()){
				String img_url = m.group(1);
				images.add(img_url);
			}
			
			download(images);
		}
	}
	
	private void download(List<String> images) {
		for(String image : images){
			String fileName = FileKit.getName(image);
			File output = new File("avatar/" + fileName);
			HttpRequest.get(image).receive(output);
			LOGGER.info(" 下载完成：" + fileName);
		}
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
		// 抓取多少页图片并下载
		new QqYou().start(5);
	}
}
