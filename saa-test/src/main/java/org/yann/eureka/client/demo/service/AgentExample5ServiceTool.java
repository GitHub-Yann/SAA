package org.yann.eureka.client.demo.service;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.util.ReflectionUtils;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;

public class AgentExample5ServiceTool {
    public static void main(String[] args) throws Exception {
        // 创建模型实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .baseUrl("http://10.1.60.160:32090/test/v1/chat")
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

        ToolCallback toolCallback = FunctionToolCallback
                .builder("currentWeather", new WeatherService())
                .description("Get the weather in location")
                .inputType(WeatherRequest.class)
                .build();

        String response = ChatClient.create(chatModel)
                .prompt("苏州的天气如何？")
                .toolCallbacks(toolCallback)
                .call()
                .content();

        System.out.println(response);
    }
}
class WeatherService implements Function<WeatherRequest, WeatherResponse> {
  public WeatherResponse apply(WeatherRequest request) {
      return new WeatherResponse(31.1, Unit.C);
  }
}

enum Unit { C, F }
record WeatherRequest(String location, Unit unit) {}
record WeatherResponse(double temp, Unit unit) {}
