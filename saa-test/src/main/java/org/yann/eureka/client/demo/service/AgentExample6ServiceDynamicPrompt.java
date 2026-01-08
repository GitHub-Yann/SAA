package org.yann.eureka.client.demo.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;

public class AgentExample6ServiceDynamicPrompt {
    public static void main(String[] args) throws Exception {
        // 创建模型实例
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("x-correlation-id","Yann202601040925-002");
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

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
            .name("micro_service_agent")
            .model(chatModel)
            .systemPrompt("你是一个微服务架构专家。如果不是微服务方面的问题，直接拒绝回答。")
            .instruction(null)
            .interceptors(new DynamicPromptInterceptor())
            .build();
        // 构造runnableConfig
        RunnableConfig config = RunnableConfig.builder().addMetadata("user_role", "beginner").build();

        // 运行 Agent
        AssistantMessage response = agent.call("帮我介绍一下网关",config);
        System.out.println(response.getText());
        System.out.println("拜拜");
    }
}

class DynamicPromptInterceptor extends ModelInterceptor {
  @Override
  public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
      // 基于上下文构建动态 system prompt
      String userRole = (String) request.getContext().getOrDefault("user_role", "default");
      String dynamicPrompt = switch (userRole) {
          case "expert" -> "你正在与技术专家对话。\n"+
                            "- 使用专业术语\n"+
                            "- 深入技术细节";
          case "beginner" -> "你正在与初学者对话。\n"+
                            "- 使用简单语言\n"+
                            "- 解释基础概念";
          default -> "你是一个专业的助手，保持友好和专业。";
      };

      SystemMessage enhancedSystemMessage;
      if (request.getSystemMessage() == null) {
          enhancedSystemMessage = new SystemMessage(dynamicPrompt);
      } else {
          enhancedSystemMessage = new SystemMessage(request.getSystemMessage().getText() + "\n" + dynamicPrompt);
      }

      ModelRequest modified = ModelRequest.builder(request)
          .systemMessage(enhancedSystemMessage)
          .build();
      return handler.call(modified);
  }

  @Override
  public String getName() {
      return "DynamicPromptInterceptor";
  }
}
