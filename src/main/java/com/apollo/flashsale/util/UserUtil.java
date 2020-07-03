package com.apollo.flashsale.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.apollo.flashsale.domain.FlashSaleUser;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生成测试用户
 */
@Slf4j
public class UserUtil {

    public static void createUser(int count) throws Exception {
        // 1.链表存储用户
        List<FlashSaleUser> users = new ArrayList<>(count);
        // 2.生成用户
        for (int i = 0; i < count; i++) {
            FlashSaleUser user = new FlashSaleUser();
            user.setId(13000000000L + i);
            user.setLoginCount(1);
            user.setNickname("user" + i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c");
            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));

            users.add(user);
        }
        log.info("用户生成完毕, 已存入链表内存中");

        // 2.插入数据库
        Connection conn = DBUtil.getConn();
        String sql = "insert into flashsale_user(id, nickname, password, salt, register_date, login_count) " +
                "values(?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        // 批量绑定
        for (int i = 0; i < users.size(); i++) {
            FlashSaleUser user = users.get(i);
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, user.getNickname());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getSalt());
            pstmt.setTimestamp(5, new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setInt(6, user.getLoginCount());

            pstmt.addBatch();
        }

        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        log.info("insert to DB");

        // 3.登录, 生成token
        String urlString = "http://localhost/flashSale/login/do_login";
        File file = new File("D:/tokens.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            // a.Request
            FlashSaleUser user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            OutputStream out = urlConnection.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFormPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = urlConnection.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            // b.Response
            String response = new String(bout.toByteArray());
            log.info("response : " + response);
            JSONObject json = JSON.parseObject(response);
            log.info(json.toJSONString());
            String token = json.getString("data");
            log.info("create token:" + user.getId());

            // 4.写入文件
            String row = user.getId() + "," + token;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            log.info("write to file : " + user.getId());
        }
        raf.close();

        log.info("over");
    }


}
