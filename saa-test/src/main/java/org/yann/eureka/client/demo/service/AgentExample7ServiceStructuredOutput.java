package org.yann.eureka.client.demo.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;

public class AgentExample7ServiceStructuredOutput {
    public static void main(String[] args) throws Exception {
        // 创建模型实例
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("x-correlation-id","Yann202601040925-006");
        DashScopeApi dashScopeApi = DashScopeApi.builder()
            .baseUrl("http://10.1.60.160:32090/test/v1/chat")
            .headers(headers)
            .apiKey("9B615417378D8769C1972D8AEE354366325224C508300D6944DFB20759C39979267FE06F60906465243B03F7187BE880AB2EFC861BBC57B93BA3DB09560351828ED40EA299D55E75DE1561BBF4A44EF3")
            .build();

        // 修改模型，默认使用qwen-plus
        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions.builder()
			.model("demo-model-v2")
            .temperature(0.2)   
            .maxToken(2000)       
            .topP(0.2)                 
			.build();
        ChatModel chatModel = DashScopeChatModel.builder()
            .dashScopeApi(dashScopeApi)
            .defaultOptions(dashScopeChatOptions)
            .build();

        // 使用 BeanOutputConverter 生成 outputSchema
        BeanOutputConverter<ContactInfo> outputConverter = new BeanOutputConverter<>(ContactInfo.class);
        String format = outputConverter.getFormat();
        
        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
            .name("contact_extractor_agent")
            .model(chatModel)
            // .outputSchema(format)
            .outputType(ContactInfo.class)
            .build();
  
        // 运行 Agent
        AssistantMessage response = agent.call("从以下信息提取联系方式：王五，wangwu@example.com，(86) 18999999998");
        System.out.println(response.getText());
        System.out.println("拜拜");
    }

    public static class ContactInfo {
        private String name;
        private String email;
        private String phone;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
