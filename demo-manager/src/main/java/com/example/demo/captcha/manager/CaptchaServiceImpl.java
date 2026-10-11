package com.example.demo.captcha.manager;

import com.example.demo.cache.api.RedisService;
import com.example.demo.captcha.api.CaptchaService;
import com.example.demo.captcha.api.bo.Captcha;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.text.impl.DefaultTextCreator;
import com.google.code.kaptcha.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import static com.google.code.kaptcha.Constants.*;

@Slf4j
@Service
public class CaptchaServiceImpl extends DefaultTextCreator implements CaptchaService {

    @Autowired
    private RedisService<String, String> redisService;

    private Producer getProducer() {
        Properties properties = new Properties();
        properties.setProperty(KAPTCHA_BORDER_COLOR, "220,223,230");
        properties.setProperty(KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL,
            "com.google.code.kaptcha.impl.ShadowGimpy");
        properties.setProperty(KAPTCHA_TEXTPRODUCER_IMPL,
            "com.example.demo.captcha.manager.CaptchaServiceImpl");
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "blue");
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "6");
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "160");
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "60");

        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(new Config(properties));

        return defaultKaptcha;
    }

    @Override
    public String getText() {
        Random random = new SecureRandom();

        int x = random.nextInt(10);
        int y = random.nextInt(10);
        int z;

        StringBuilder sb = new StringBuilder();

        int r = random.nextInt(3);

        if (r == 0) {
            z = x * y;
            sb.append(x).append("*").append(y);
        } else if (r == 1) {
            if (x != 0 && y % x == 0) {
                z = y / x;
                sb.append(y).append("/").append(x);
            } else {
                z = x + y;
                sb.append(x).append("+").append(y);
            }
        } else {
            if (x >= y) {
                z = x - y;
                sb.append(x).append("-").append(y);
            } else {
                z = y - x;
                sb.append(y).append("-").append(x);
            }
        }

        sb.append("=?@").append(z);

        return sb.toString();
    }

    @Override
    public Captcha getCaptcha() {
        Producer producer = getProducer();

        String uuid = UUID.randomUUID().toString();
        String[] text = producer.createText().split("=?@");
        BufferedImage image = producer.createImage(text[0]);

        try {
            redisService.add(RedisService.CACHE_KEY_CAPTCHA + uuid, text[1],
                RedisService.CACHE_KEY_CAPTCHA_DEFAULT_EXP);
        } catch (ServiceException e) {
            log.error("{},{}", uuid, text, e);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "系统正忙，请稍后再试");
        }

        FastByteArrayOutputStream stream = new FastByteArrayOutputStream();

        try {
            ImageIO.write(image, "jpg", stream);
        } catch (IOException e) {
            log.error("{},{}", uuid, text, e);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "系统正忙，请稍后再试");
        }

        return new Captcha(uuid, Base64.getEncoder().encodeToString(stream.toByteArray()));
    }

    @Override
    public void validate(String uuid, String code) {
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(code)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "验证码不能为空");
        }

        String key = RedisService.CACHE_KEY_CAPTCHA + uuid;

        String text = redisService.get(key);

        remove(key);

        if (StringUtils.isBlank(text)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "验证码已失效");
        }

        if (!text.equalsIgnoreCase(code)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "验证码错误");
        }
    }

    private void remove(String key) {
        try {
            redisService.remove(key);
        } catch (Exception e) {
            log.error("{}", key, e);
        }
    }

}
