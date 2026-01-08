package org.yann.eureka.client.demo.service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.ReflectionUtils;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;

/**
 * tools 的使用示例
 */
public class AgentExample4ServiceTool {
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

        // 声明式，使用 @Tool 注解
        String response1 = ChatClient.create(chatModel)
                .prompt("后天几号？")
                .tools(new DateTimeTools())
                .call()
                .content();

        System.out.println(response1);

        // 编程式，使用低级 MethodToolCallback 实现
        Method method = ReflectionUtils.findMethod(DateTime2Tools.class, "getCurrentDateTime");
        ToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinitions.builder(method)
                        .description("Get the current date and time in the user's timezone")
                        .build())
                .toolMethod(method)
                .toolObject(new DateTime2Tools())   // 如果方法是静态的，您可以省略
                .build();

        String response = ChatClient.create(chatModel)
                .prompt("后天几号？")
                .toolCallbacks(toolCallback)
                .call()
                .content();

        System.out.println(response);
    }
}

class DateTime2Tools {

//     @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

}

class DateTimeTools {

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

}
