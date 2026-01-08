package org.yann.eureka.client.demo.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;

public class AgentExample1ServiceReactAgent {
    public static void main(String[] args) throws Exception {
        // 创建模型实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
            .baseUrl("http://10.1.60.160:32090/test/v1/chat")
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
            .instruction(null)
            .build();

        // 运行 Agent
        AssistantMessage response = null;
        while(true){
            System.out.print("请输入您的问题：");
            String question = System.console().readLine();
            if("exit".equals(question)){
                break;
            }
            response = agent.call(question);
            System.out.println(response.getText());
        }
        System.out.println("拜拜");
    }
}
