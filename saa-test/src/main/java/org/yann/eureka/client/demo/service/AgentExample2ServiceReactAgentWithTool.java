package org.yann.eureka.client.demo.service;

import java.util.Random;
import java.util.function.BiFunction;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;

public class AgentExample2ServiceReactAgentWithTool {

    public static void main(String[] args) {
        // 创建模型实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
            .baseUrl("http://10.1.60.160:32090/test/v1/chat")
            .apiKey("9B615417378D8769C1972D8AEE354366325224C508300D6944DFB20759C39979267FE06F60906465243B03F7187BE880AB2EFC861BBC57B93BA3DB09560351828ED40EA299D55E75DE1561BBF4A44EF3")
            .build();

        // 修改模型，默认使用qwen-plus
        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions.builder()
			.model("demo-model-v2")
			.build();
        ChatModel chatModel = DashScopeChatModel.builder()
            .dashScopeApi(dashScopeApi)
            .defaultOptions(dashScopeChatOptions)
            .build();

        // 定义工具
        ToolCallback populationTool = FunctionToolCallback.builder("get_pupulation", new PopulationTool())
                                        .description("Get population for a given city")
                                        .inputType(String.class)
                                        .build();

        // 扩展功能
        Hook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("get_pupulation", ToolConfig.builder().description("Please confirm tool execution.").build())
                .build();
        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
            .name("population_agent")
            .model(chatModel)
            .tools(populationTool)
            .hooks(humanInTheLoopHook)
            .systemPrompt("你是一个人口查询专家。了解世界各地城市的人口数据。能够回答有关人口的问题。如果不是人口问题，直接拒绝回答。")
            .instruction(null)
            .saver(new MemorySaver())  // 为agent 添加记忆
            .build();

        // 运行 Agent
        AssistantMessage response = null;
        while(true){
            System.out.print("请输入您的问题：");
            String question = System.console().readLine();
            if("exit".equals(question)){
                break;
            }
            try {
                response = agent.call(question);
                System.out.println(response.getText());
            } catch (GraphRunnerException e) {
                System.out.println("啊噢，出错了！"+e.getMessage());
            }
        }
        System.out.println("拜拜");
    }

    static class PopulationTool implements BiFunction<String,ToolContext,String>{
        @Override
        public String apply(String city, ToolContext toolContext) {
            Random random = new Random();
            int population = 1000+random.nextInt(200);
            return city+"的人口大约："+population+"万";
        }
    }
}

