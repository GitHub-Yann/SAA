package org.yann.eureka.client.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;

public class AgentExample8ServiceMemoryUpdate {
    public static void main(String[] args) throws Exception {
        // 创建模型实例
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("x-correlation-id","Yann202601071431-002");
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

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
            .name("mathematics_agent")
            .model(chatModel)
            .systemPrompt("你是一个数学小能手。了解各种数学知识。能够解答数学相关的问题。如果不是数学问题，直接拒绝回答。")
            .saver(new MemorySaver())
            .hooks(new CustomMemoryUpdateHook())
            .build();

        // 使用 thread_id 维护对话上下文
        RunnableConfig config = RunnableConfig.builder()
            .threadId("talk-1") // threadId 指定会话 ID
            .build();
        // 运行 Agent
        AssistantMessage response = null;
        while(true){
            System.out.print("请输入您的问题：");
            String question = System.console().readLine();
            if("exit".equals(question)){
                break;
            }
            response = agent.call(question, config);
            System.out.println(response.getText());
        }
        System.out.println("拜拜");
    }
}

class CustomMemoryUpdateHook extends MessagesModelHook {

    private static final int MAX_MESSAGES = 3;

    @Override
    public String getName() {
        return "custom_memory_update_hook";
    }

    @Override
    public AgentCommand afterModel(List<Message> previousMessages, RunnableConfig config) {
        System.out.println("after model......");
        return super.afterModel(previousMessages, config);
    }

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        if (previousMessages.size() <= MAX_MESSAGES) {
            return new AgentCommand(previousMessages); // 无需更改
        }

        // 保留第一条消息和最后几条消息
        Message firstMsg = previousMessages.get(0);
        int keepCount = previousMessages.size() % 2 == 0 ? 3 : 4;
        List<Message> recentMessages = previousMessages.subList(
                previousMessages.size() - keepCount,
                previousMessages.size());

        List<Message> trimmedMessages = new ArrayList<>();
        trimmedMessages.add(firstMsg);
        trimmedMessages.addAll(recentMessages);

        // 使用 REPLACE 策略替换消息列表，只保留需要的消息
        return new AgentCommand(trimmedMessages, UpdatePolicy.REPLACE);
    }

    @Override
    public HookPosition[] getHookPositions() {
        return new HookPosition[]{HookPosition.BEFORE_MODEL};
    }

    

}
