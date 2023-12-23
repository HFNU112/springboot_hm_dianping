package com.hmdp;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @Author Husp
 * @Date 2023/12/17 1:06
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class TestMultiUserTokens {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    IUserService userService;

    @Autowired
    ObjectMapper mapper;

    @Test
    @DisplayName("测试成功生成1000个token表示不同的用户")
    void testGenerotorTokens() throws Exception {
        //查询数据库得到1000个手机号 SELECT u.id, u.phone  FROM tb_user u LIMIT 0, 5
        List<String> phones = userService.lambdaQuery()
                .select(User::getPhone)
                .last("LIMIT 10, 1000")
                .list().stream().map(User::getPhone).collect(Collectors.toList());
        //1000个手机号存到线程池并指定线程池大小
        ExecutorService executorService = ThreadUtil.newExecutor(phones.size());

        //存储生成token，CopyOnWriteArrayList保证线程安全
        List<String> tokens = new CopyOnWriteArrayList<>();
        //创建线程计数器
        CountDownLatch countDownLatch = new CountDownLatch(phones.size());
        //遍历手机号集合
        phones.forEach(phone -> {
            executorService.execute(() -> {
                try {
                    //mock获取验证码
                    String codeStr = mockMvc.perform(MockMvcRequestBuilders
                            .post("/user/code")
                            .queryParam("phone", phone))
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andReturn().getResponse().getContentAsString();

                    //反序列化Result对象
                    Result result = mapper.readerFor(Result.class).readValue(codeStr);
                    if (ObjectUtil.isEmpty(result)) {
                        return;
                    }
//                    Assertions.assertTrue(result.getSuccess(), String.format("手机号%s获取验证码成功", phone));
                    String code = result.getData().toString();

                    //建造者模式创建Login登录对象
//                    LoginFormDTO loginFormDTO = loginBuilder.build();
                    LoginFormDTO loginFormDTO = new LoginFormDTO();
                    loginFormDTO.setCode(phone);
                    loginFormDTO.setCode(code);
                    //LoginFormDTO对象序列化为JSON
                    String loginStr = mapper.writeValueAsString(loginFormDTO);

                    //mock登录请求
                    String tokenStr = mockMvc.perform(MockMvcRequestBuilders
                            .post("/user/login")
                            .content(loginStr)
                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andReturn().getResponse().getContentAsString();
                    //反序列化为Result对象
                    result = mapper.readerFor(Result.class).readValue(tokenStr);
//                    Assertions.assertTrue(result.getSuccess(), String.format("手机号%s获取token成功", phone, loginStr));
                    String token = result.getData().toString();
                    //token添加到集合中
                    tokens.add(token);
                    //线程计数器减1
                    countDownLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        //线程计数器为0唤醒主线程
        countDownLatch.await();
        //关闭线程池
        executorService.shutdown();
        Assertions.assertEquals(phones.size(), tokens.size());
        //1000个token写到文件中
        writeToken(tokens, "\\tokens.txt");
        log.info("线程执行完成！");
    }

    private void writeToken(List<String> tokens, String suffixPath) throws IOException {
        File file = new File("src\\main\\resources" + suffixPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        //创建输出流
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));

        for (String token : tokens) {
            bufferedWriter.write(token);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
        log.info("文件写入完成！");
    }

}
