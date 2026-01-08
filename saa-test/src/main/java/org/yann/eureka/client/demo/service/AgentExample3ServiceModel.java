package org.yann.eureka.client.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;

import reactor.core.publisher.Flux;

public class AgentExample3ServiceModel {
    public static void main(String[] args) throws Exception {
        // 创建模型实例
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("x-correlation-id","Yann202512311333-001");
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .baseUrl("http://10.1.60.160:32090/test/v1/chat")
                .headers(headers)
                .apiKey("9B615417378D8769C1972D8AEE354366325224C508300D6944DFB20759C39979267FE06F60906465243B03F7187BE880AB2EFC861BBC57B93BA3DB09560351828ED40EA299D55E75DE1561BBF4A44EF3")
                .build();

        // 修改模型，默认使用qwen-plus
        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions.builder()
                .model("demo-model-v2")
                .temperature(0.5)
                .maxToken(2000)
                .topP(0.4)
                .build();
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(dashScopeChatOptions)
                .build();

        // 简单调用
        // String response = chatModel.call("你好，你是谁？");
        // System.out.println(response);

        // 使用prompt
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个古诗鉴赏助手。"));
        messages.add(new UserMessage("鉴赏一下贾岛的《寻隐者不遇》"));
        Prompt prompt = Prompt.builder()
                .messages(messages)
                .build();
        // 正常响应
        ChatResponse response = chatModel.call(prompt);
        System.out.println(response.getResult().getOutput().getText());
        // Flux<ChatResponse> response = chatModel.stream(prompt);
        // // 订阅并处理流式响应
        // final StringBuilder tkUsage = new StringBuilder("0");
        // final StringBuilder isCompleted = new StringBuilder("false");
        // response.subscribe(
        //         chatResponse -> {
        //             String content = chatResponse.getResult()
        //                     .getOutput()
        //                     .getText();
        //             if (chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null &&
        //                     chatResponse.getMetadata().getUsage().getTotalTokens() != null && chatResponse.getMetadata()
        //                             .getUsage().getTotalTokens().compareTo(Integer.valueOf(tkUsage.toString())) > 0) {
        //                 tkUsage.setLength(0);
        //                 tkUsage.append(chatResponse.getMetadata().getUsage().getTotalTokens());
        //             }
        //             System.out.print(content);
        //         },
        //         error -> {
        //             System.err.println("错误: " + error.getMessage());
        //             isCompleted.setLength(0);
        //             isCompleted.append("true");
        //         },
        //         () -> {
        //             System.out.println("\n共计消耗token：" + tkUsage.toString());
        //             isCompleted.setLength(0);
        //             isCompleted.append("true");
        //         });
        // while (!Boolean.parseBoolean(isCompleted.toString())) {
        //     Thread.sleep(2000);
        // }
        System.out.println("结束");
    }
}
