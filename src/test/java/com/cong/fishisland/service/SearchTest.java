package com.cong.fishisland.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cong.fishisland.common.TestBaseByLogin;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class SearchTest extends TestBaseByLogin {
    @Test
    void searchZhiHuData() {
        String urlZhiHu = "https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total?limit=50&desktop=true";

        String result = HttpRequest.get(urlZhiHu).execute().body();
        JSONObject resultJson = (JSONObject) JSON.parse(result);
        JSONArray data = resultJson.getJSONArray("data");
        data.forEach(item -> {
            JSONObject jsonItem = (JSONObject) item;
            JSONObject target = jsonItem.getJSONObject("target");
            String title = target.getString("title");
            String url = target.getString("url");
            String followerCount = target.getString("follower_count");
            String excerpt = target.getString("excerpt");
            log.info("\n标题：{}，\n链接：{}，\n热度：{} 万，\n摘要：{}", title, url, followerCount, excerpt);

        });

    }

    @Test
    void weiboSearchTest() throws IOException {
        //获取tid
        String tidUrl = "https://passport.weibo.com/visitor/genvisitor";
        Map<String, Object> params = new HashMap<>();
        params.put("cb", "gen_callback");
        String str = HttpUtil.get(tidUrl, params, 3000);
        String quStr = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
        String tid = "";
        if (!quStr.isEmpty()) {
            JSONObject result = JSONObject.parseObject(quStr);
            if (result.getIntValue("retcode") == 20000000) {
                tid = result.getJSONObject("data").getString("tid");
                System.out.println("tid:" + tid);
            }
        }

        //获腹SUb,sUbp
        String subUrl = "https://passport.weibo.com/visitor/visitor";
        Map<String, Object> params2 = new HashMap<>();
        params2.put("a", "incarnate");
        params2.put("t", tid);
        params2.put("w", "3");
        params2.put("c", "100");
        params2.put("cb", "cross_domain");
        params2.put("from", "weibo");
        String str2 = HttpUtil.get(subUrl, params2, 3000);
        String resultStr = str2.substring(str2.indexOf("(") + 1, str2.indexOf(")"));
        String sub = "";
        String subp = "";
        if (!resultStr.isEmpty()) {
            JSONObject result = JSONObject.parseObject(resultStr);
            if (result.getIntValue("retcode") == 20000000) {
                sub = result.getJSONObject("data").getString("sub");
                subp = result.getJSONObject("data").getString("subp");
            }
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        HttpGet request = new HttpGet("https://s.weibo.com/top/summary?cate=realtimehot");
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        request.setHeader("Referer", "https://s.weibo.com/top/summary?cate=realtimehot");
        request.setHeader("Cookie", "SUB=" + sub + "; SUBP=" + subp + ";");

        response = httpClient.execute(request);
        String html = EntityUtils.toString(response.getEntity());

        Document document = Jsoup.parse(html);
        Element item = document.getElementsByTag("tbody").first();
        if (item != null) {
            Elements items = item.getElementsByTag("tr");
            for (Element tmp : items) {
                Element rankEle = tmp.getElementsByTag("td").first();
                Elements textEle = tmp.select(".td-02").select("a");
                Elements followerEle = tmp.select(".td-02").select("span");
                //过滤广告
                Elements rdEle = tmp.select(".td-02").select("span");
                if (!Objects.requireNonNull(rankEle).text().isEmpty() && !rdEle.text().isEmpty()) {
                    log.info("title: {}", textEle.text());
                    log.info("url: https://s.weibo.com{}", textEle.attr("href"));
                    log.info("followerCount: {}", followerEle.text());
                }
            }
        }
    }


}
