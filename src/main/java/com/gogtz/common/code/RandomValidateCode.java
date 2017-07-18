package com.gogtz.common.code;

import com.gogtz.common.cookie.CookieUtils;
import com.gogtz.common.session.SessionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * <h1>生成随机数的工具类</h1>
 *
 * @author t
 * @Time 2017-7-17 15:02:53
 */
public class RandomValidateCode {
    private Random random = new Random();
    //    private String randString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";// 随机产生的字符串
    private String randString = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";// 随机产生的字符串
    private String RANDOMCODEKEY = "RANDOMCODEKEY_";
    private String cookieId = "SHAREJSESSIONID";

    private int width = 118;// 图片宽
    private int height = 42;// 图片高
    private int lineSize = 150;// 干扰线数量
    private int stringNum = 4;// 随机产生字符数量

    /**
     * 构造函数
     */
    public RandomValidateCode() {
    }

    /**
     * 构造函数
     *
     * @param cookieId cookieId
     */
    public RandomValidateCode(String cookieId) {
        this.cookieId = cookieId;
    }

    /**
     * 获得字体
     *
     * @return 字体(Fixedsys)
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.CENTER_BASELINE, 30);
    }

    /**
     * 获得颜色
     *
     * @param fc fc
     * @param bc bc
     * @return 返回颜色
     */
    private Color getRandColor(int fc, int bc) {
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc - 16);
        int g = fc + random.nextInt(bc - fc - 14);
        int b = fc + random.nextInt(bc - fc - 18);
        return new Color(r, g, b);
    }

    /**
     * 生成随机图片
     *
     * @param request  request
     * @param response response
     * @param pre      前缀
     */
    public void getRandcode(HttpServletRequest request, HttpServletResponse response, String pre) {
        response.setContentType("image/jpeg");// 设置相应类型,告诉浏览器输出的内容为图片
        response.setHeader("Pragma", "No-cache");// 设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);

        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics2D g = (Graphics2D) image.getGraphics();// 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
        Graphics2D g2 = (Graphics2D) g;//强制类型转换
        Color color = new Color(245, 90, 105);
        g2.setColor(color);//设置当前绘图颜色
        Shape rect = new Rectangle2D.Double(0, 0, width, height);
        g2.fill(rect);//填充矩形

        //        Color color = new Color(245, 90, 105);
        //        g.setPaint(color);
        //        g.fillRect(0, 0, width, height);
        //        g.setColor(Color.WHITE);
        //        g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 30));
        //        // 绘制干扰线
        //        for (int i = 0; i <= lineSize; i++) {
        //            drowLine(g);
        //        }
        // 绘制随机字符
        String randomString = "";
        for (int i = 1; i <= stringNum; i++) {
            randomString = drowString(g, randomString, i);
        }
        Cookie jSessionIdCookie = CookieUtils.getCookieByName(request, cookieId);
        if (jSessionIdCookie != null) {
            //RedisUtils.set("phpSessionId_" + phpSessionIdCookie.getValue(), randomString, 10 * 60);//10分钟有效
            SessionUtils.setSession(pre + RANDOMCODEKEY + jSessionIdCookie.getValue(), randomString);
        }
        g.dispose();
        try {
            ImageIO.write(image, "JPEG", response.getOutputStream());// 将内存中的图片通过流动形式输出到客户端
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制字符串
     *
     * @param g            图形
     * @param randomString 随机数
     * @param i            i
     * @return 返回随机数
     */
    private String drowString(Graphics g, String randomString, int i) {
        g.setFont(getFont());
        g.setColor(Color.white);//设置当前绘图颜色
        String rand = String.valueOf(getRandomString(random.nextInt(randString.length())));
        randomString += rand;
        g.translate(random.nextInt(3), random.nextInt(3));
        g.drawString(rand, 20 * i, 30);
        return randomString;
    }

    /**
     * 绘制干扰线
     *
     * @param g g
     */
    private void drowLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /**
     * 获取随机的字符
     *
     * @param num num
     * @return
     */
    public String getRandomString(int num) {
        return String.valueOf(randString.charAt(num));
    }

    /**
     * 检查图片验证码
     *
     * @param request request
     * @param randomCode randomCode
     * @return
     */
    public boolean checkRandomCode(HttpServletRequest request, String randomCode, String pre) {
        Cookie jSessionIdCookie = CookieUtils.getCookieByName(request, cookieId);
        if (jSessionIdCookie != null) {
            String oldCode = SessionUtils.getSession(pre + RANDOMCODEKEY + jSessionIdCookie.getValue());
            if (oldCode != null && StringUtils.equalsIgnoreCase(oldCode, randomCode)) {
                return true;
            }
        }
        return false;
    }
}
