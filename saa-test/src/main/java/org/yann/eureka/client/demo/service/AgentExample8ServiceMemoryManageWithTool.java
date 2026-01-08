package org.yann.eureka.client.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.StoreSearchRequest;
import com.alibaba.cloud.ai.graph.store.StoreSearchResult;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;

import static com.alibaba.cloud.ai.graph.agent.tools.ToolContextConstants.AGENT_CONFIG_CONTEXT_KEY;

public class AgentExample8ServiceMemoryManageWithTool {

    // 定义请求和响应记录
    public record GetMemoryRequest(List<String> namespace, String key) {}
    public record MemoryResponse(String message, Map<String, Object> value) {}
    public record SaveMemoryRequest(List<String> namespace, String key, Map<String, Object> value) {}

    public static MemoryStore store = new MemoryStore();
    public static ChatModel chatModel;

    public static void main(String[] args) throws Exception {
        AgentExample8ServiceMemoryManageWithTool.init();
        System.out.println("初始化完成，第一次获取用户信息");
        AgentExample8ServiceMemoryManageWithTool.testGetLlmCall();
        System.out.println("测试保存用户信息");
        AgentExample8ServiceMemoryManageWithTool.testSaveLlmCall();
        System.out.println("第二次获取用户信息");
        AgentExample8ServiceMemoryManageWithTool.testGetLlmCall();
    }

    public static void init(){
        // 创建模型实例
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("x-correlation-id","Yann202601081727-002");
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
        chatModel = DashScopeChatModel.builder()
            .dashScopeApi(dashScopeApi)
            .defaultOptions(dashScopeChatOptions)
            .build();
    }

    // 工具中获取长期记忆
    public static void testGetLlmCall() throws Exception{
        // 获取用户的工具
        BiFunction<GetMemoryRequest, ToolContext, MemoryResponse> getUserInfoMemoryFunction = (request, context) -> {
            RunnableConfig runnableConfig = (RunnableConfig) context.getContext().get(AGENT_CONFIG_CONTEXT_KEY);
            Store store = runnableConfig.store();
            Optional<StoreItem> itemOpt = store.getItem(request.namespace(), request.key());
            if (itemOpt.isPresent()) {
                Map<String, Object> value = itemOpt.get().getValue();
                return new MemoryResponse("找到用户信息", value);
            }
            return new MemoryResponse("未找到用户", Map.of());
        };
        ToolCallback getUserInfoTool = FunctionToolCallback.builder("getUserInfo", getUserInfoMemoryFunction)
            .description("查询用户信息")
            .inputType(GetMemoryRequest.class)
            .build();

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
            .name("user_agent")
            .model(chatModel)
            .systemPrompt("你是一个用户信息记忆小能手。")
            // .saver(new MemorySaver())
            .tools(List.of(getUserInfoTool))
            .build();

        // 使用 thread_id 维护对话上下文
        RunnableConfig config = RunnableConfig.builder()
            .threadId("session_001")
            .addMetadata("user_id", "user_123")
            .store(store)
            .build();
        // 运行 Agent
        AssistantMessage response = agent.call("查询用户信息，namespace=['users'], key='user_123'", config);
        System.out.println(response.getText());
    }

    // 工具中保存长期记忆
    public static void testSaveLlmCall() throws Exception{
        // 创建保存用户信息的工具
        BiFunction<SaveMemoryRequest, ToolContext, MemoryResponse> saveUserInfoFunction = (request, context) -> {
            RunnableConfig runnableConfig = (RunnableConfig) context.getContext().get(AGENT_CONFIG_CONTEXT_KEY);
            Store store = runnableConfig.store();
            StoreItem item = StoreItem.of(request.namespace(), request.key(), request.value());
            store.putItem(item);
            return new MemoryResponse("成功保存用户信息", request.value());
        };

        ToolCallback saveUserInfoTool = FunctionToolCallback.builder("saveUserInfo", saveUserInfoFunction)
        .description("保存用户信息")
        .inputType(SaveMemoryRequest.class)
        .build();

        RunnableConfig config = RunnableConfig.builder()
            .threadId("session_002")
            .addMetadata("user_id", "user_123")
            .store(store)
            .build();
 
        // 创建Agent
        ReactAgent agent = ReactAgent.builder()
            .name("save_memory_agent")
            .model(chatModel)
            .tools(saveUserInfoTool)
            // .saver(new MemorySaver())
            .build();
        AssistantMessage response = agent.call("保存用户信息，namespace=['users'], key='user_123', value={'name': 'Yann', 'age': 18}", config);
        System.out.println(response.getText());
    }

    // 测试内存存储
    public static void testMemoryStore(){
        // MemoryStore 将数据保存到内存字典中。在生产环境中请使用基于数据库的存储实现
        MemoryStore store = new MemoryStore();

        String userId = "my-user";
        String applicationContext = "chitchat";
        List<String> namespace = List.of(userId, applicationContext);

        // 保存记忆
        Map<String, Object> memoryData1 = new HashMap<>();
        memoryData1.put("rules", List.of(
                "用户喜欢简短直接的语言",
                "用户只说中文和Java"));
        memoryData1.put("my-key", "my-value");

        StoreItem item1 = StoreItem.of(namespace, "memory-2025", memoryData1);
        store.putItem(item1);

        Map<String, Object> memoryData2 = new HashMap<>();
        memoryData2.put("profiles", List.of(
                "资深Java工程师",
                "微服务架构师"));
        memoryData2.put("my-key", "my-value");

        StoreItem item2 = StoreItem.of(namespace, "memory-2026", memoryData2);
        store.putItem(item2);

        // 通过ID获取记忆
        Optional<StoreItem> retrievedItem = store.getItem(namespace, "memory-2025");
        System.out.println("1--->"+retrievedItem);
        // 在此命名空间内搜索记忆，通过内容等价性过滤，按向量相似度排序
        StoreSearchResult storeSearchResult = store.searchItems(StoreSearchRequest.builder().namespace(namespace).filter(Map.of("my-key", "my-value")).build());
        List<StoreItem> items = storeSearchResult.getItems();
        items.forEach(itemX -> System.out.println("2--->"+itemX));
    }

}
