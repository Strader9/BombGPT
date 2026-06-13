package com.campus.campus_life.service.impl;

import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.mapper.KnowledgeMapper;
import com.campus.campus_life.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.url}")
    private String aiUrl;

    @Value("${ai.model}")
    private String aiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private KnowledgeMapper knowledgeMapper;

    @Override
    public String chat(String question) {
        try {
            if (question == null || question.trim().isEmpty()) {
                return "请先输入你的问题。";
            }

            // 1. 从前端传来的完整上下文里，提取用户当前真正的问题
            String currentQuestion = extractCurrentQuestion(question);

            // 系统自检类问题，不交给大模型乱猜，直接查真实后端状态
            if (isSystemCheckQuestion(currentQuestion)) {
                return buildSystemCheckAnswer();
            }

            // 2. 让模型根据上下文，把追问/口语表达改写成适合检索知识库的问题
            String rewrittenQuestion = rewriteQuestionByContext(question, currentQuestion);

            // 3. 根据用户表达生成多个检索词
            List<String> searchQueries = buildSearchQueries(
                    question,
                    currentQuestion,
                    rewrittenQuestion
            );

            // 4. 用多个检索词查知识库，并按“相同问题 > 高度相似问题 > 普通相关问题”排序
            List<Knowledge> knowledgeList = searchKnowledgeByQueries(
                    searchQueries,
                    currentQuestion,
                    rewrittenQuestion
            );

            // 5. 如果知识库命中，必须把数据库答案交给 Ollama，让 Ollama 基于数据库答案做人性化回答
            if (knowledgeList != null && !knowledgeList.isEmpty()) {
                String knowledgeText = buildKnowledgeText(knowledgeList);
                return answerWithKnowledge(question, currentQuestion, rewrittenQuestion, knowledgeText);
            }

            // 6. 数据库完全没有命中时，才正常调用模型自由回答
            return askOllamaNormally(question);

        } catch (Exception e) {
            e.printStackTrace();
            return "AI模型调用失败，请检查 Ollama 是否已启动，以及模型名称是否正确。";
        }
    }

    /**
     * 从前端拼接的上下文中提取“用户现在的问题”
     */
    private String extractCurrentQuestion(String fullQuestion) {
        if (fullQuestion == null) {
            return "";
        }

        String marker = "用户现在的问题是：";

        if (fullQuestion.contains(marker)) {
            String after = fullQuestion.substring(fullQuestion.indexOf(marker) + marker.length());

            String endMarker = "请结合上下文回答用户现在的问题。";
            if (after.contains(endMarker)) {
                after = after.substring(0, after.indexOf(endMarker));
            }

            return after.trim();
        }

        return fullQuestion.trim();
    }

    /**
     * 根据历史上下文和用户当前表达，把问题改写成适合知识库检索的独立问题。
     */
    private String rewriteQuestionByContext(String fullQuestion, String currentQuestion) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content",
                                    "你是校园知识库检索问题改写助手。" +
                                            "你的任务是根据历史上下文和用户当前表达，改写成一个完整、明确、适合检索校园知识库的问题。" +
                                            "回答不要以“明白了”“好的”“当然”“可以”“没问题”等客套话开头，直接回答重点。" +
                                            "如果用户说“我饿了”“想吃饭”“肚子饿”“吃什么”，应理解为咨询食堂、餐饮、饭堂、吃饭地点、营业时间等信息。" +
                                            "如果用户说“不舒服”“生病了”，应理解为咨询校医院、医疗、就医等信息。" +
                                            "如果用户说“卡丢了”“饭卡没了”，应理解为咨询校园卡挂失、补办等信息。" +
                                            "如果用户说“宿舍坏了”“灯坏了”“空调坏了”，应理解为咨询宿舍报修。" +
                                            "只输出改写后的问题，不要解释，不要回答。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content",
                                    "完整上下文：\n" + fullQuestion +
                                            "\n\n用户当前问题：\n" + currentQuestion +
                                            "\n\n请输出适合检索校园知识库的独立问题："
                            )
                    ),
                    "stream", false
            );

            String result = callOllama(requestBody);

            if (result == null || result.trim().isEmpty()) {
                return currentQuestion;
            }

            return result.trim()
                    .replace("改写后的问题：", "")
                    .replace("独立问题：", "")
                    .replace("检索问题：", "")
                    .trim();

        } catch (Exception e) {
            return currentQuestion;
        }
    }

    /**
     * 构造多个检索词。
     */
    private List<String> buildSearchQueries(String fullQuestion, String currentQuestion, String rewrittenQuestion) {
        List<String> queries = new ArrayList<>();

        addQuery(queries, currentQuestion);
        addQuery(queries, rewrittenQuestion);

        String text = (
                (fullQuestion == null ? "" : fullQuestion) + " " +
                        (currentQuestion == null ? "" : currentQuestion) + " " +
                        (rewrittenQuestion == null ? "" : rewrittenQuestion)
        ).toLowerCase();

        // 餐饮/食堂意图
        if (containsAny(text, List.of("饿", "吃饭", "吃东西", "饭", "食堂", "饭堂", "餐厅", "餐饮", "早餐", "午餐", "晚餐", "夜宵", "菜", "食物", "美食", "堂食", "二饭", "一饭"))) {
            addQuery(queries, "二饭可以堂食吗");
            addQuery(queries, "二饭堂食");
            addQuery(queries, "堂食");
            addQuery(queries, "二饭");
            addQuery(queries, "食堂");
            addQuery(queries, "餐饮");
            addQuery(queries, "吃饭");
            addQuery(queries, "饭堂");
            addQuery(queries, "食堂开放时间");
            addQuery(queries, "食堂位置");
        }

        // 医疗/校医院意图
        if (containsAny(text, List.of("不舒服", "生病", "发烧", "感冒", "头疼", "肚子疼", "受伤", "校医院", "看病", "就医", "医生"))) {
            addQuery(queries, "校医院");
            addQuery(queries, "医疗");
            addQuery(queries, "就医");
            addQuery(queries, "看病");
        }

        // 校园卡意图
        if (containsAny(text, List.of("校园卡", "饭卡", "卡丢", "卡没了", "挂失", "补办", "一卡通", "充值"))) {
            addQuery(queries, "校园卡");
            addQuery(queries, "挂失");
            addQuery(queries, "补办");
            addQuery(queries, "充值");
        }

        // 宿舍报修意图
        if (containsAny(text, List.of("宿舍", "坏了", "报修", "空调", "水电", "灯坏", "门坏", "维修"))) {
            addQuery(queries, "宿舍报修");
            addQuery(queries, "报修");
            addQuery(queries, "后勤");
        }

        // 图书馆意图
        if (containsAny(text, List.of("图书馆", "借书", "还书", "续借", "自习", "座位", "文献"))) {
            addQuery(queries, "图书馆");
            addQuery(queries, "借书");
            addQuery(queries, "续借");
            addQuery(queries, "自习");
        }

        // 教务/课程/考试意图
        if (containsAny(text, List.of("选课", "成绩", "考试", "课表", "教务", "补考", "缓考", "学分", "注册", "开学", "放假"))) {
            addQuery(queries, "教务");
            addQuery(queries, "选课");
            addQuery(queries, "考试");
            addQuery(queries, "成绩");
        }

        // 再从当前问题里提取 2~6 字片段，增强相似检索
        addQuestionFragments(queries, currentQuestion);
        addQuestionFragments(queries, rewrittenQuestion);

        return queries;
    }

    /**
     * 用多个检索词查知识库，并按相似度排序。
     */
    private List<Knowledge> searchKnowledgeByQueries(
            List<String> queries,
            String currentQuestion,
            String rewrittenQuestion
    ) {
        Map<Long, Knowledge> resultMap = new LinkedHashMap<>();

        for (String query : queries) {
            if (query == null || query.trim().isEmpty()) {
                continue;
            }

            List<Knowledge> list = knowledgeMapper.searchForAi(query.trim());

            if (list == null || list.isEmpty()) {
                continue;
            }

            for (Knowledge knowledge : list) {
                if (knowledge == null || knowledge.getId() == null) {
                    continue;
                }

                resultMap.putIfAbsent(knowledge.getId(), knowledge);
            }

            if (resultMap.size() >= 80) {
                break;
            }
        }

        List<Knowledge> candidates = new ArrayList<>(resultMap.values());

        if (candidates.isEmpty()) {
            return candidates;
        }

        List<KnowledgeScore> scoredList = candidates.stream()
                .map(k -> new KnowledgeScore(k, calculateKnowledgeScore(k, currentQuestion, rewrittenQuestion, queries)))
                .filter(item -> item.score > 0)
                .sorted(Comparator.comparingInt((KnowledgeScore item) -> item.score).reversed())
                .collect(Collectors.toList());

        if (scoredList.isEmpty()) {
            return new ArrayList<>();
        }

        /*
         * 关键逻辑：
         * 如果最高分是完全相同问题，说明数据库里有精准答案。
         * 此时只把这一条交给 Ollama，避免 Ollama 被其他泛化知识干扰。
         */
        KnowledgeScore best = scoredList.get(0);
        if (best.score >= 10000) {
            List<Knowledge> exactOnly = new ArrayList<>();
            exactOnly.add(best.knowledge);
            return exactOnly;
        }

        /*
         * 如果没有完全相同问题，就取最相似的前 5 条。
         */
        List<Knowledge> result = new ArrayList<>();

        for (KnowledgeScore item : scoredList) {
            if (result.size() >= 5) {
                break;
            }

            result.add(item.knowledge);
        }

        return result;
    }

    /**
     * 计算某条知识与用户问题的相似度分数。
     */
    private int calculateKnowledgeScore(
            Knowledge knowledge,
            String currentQuestion,
            String rewrittenQuestion,
            List<String> queries
    ) {
        if (knowledge == null) {
            return 0;
        }

        String dbQuestion = safe(knowledge.getQuestion());
        String dbAnswer = safe(knowledge.getAnswer());
        String dbKeywords = safe(knowledge.getKeywords());

        String normalizedDbQuestion = normalizeText(dbQuestion);
        String normalizedCurrent = normalizeText(currentQuestion);
        String normalizedRewritten = normalizeText(rewrittenQuestion);

        int score = 0;

        // 1. 完全相同问题，最高优先级
        if (!normalizedCurrent.isEmpty() && normalizedDbQuestion.equals(normalizedCurrent)) {
            score += 10000;
        }

        if (!normalizedRewritten.isEmpty() && normalizedDbQuestion.equals(normalizedRewritten)) {
            score += 9500;
        }

        // 2. 高度包含关系
        if (!normalizedCurrent.isEmpty()) {
            if (normalizedDbQuestion.contains(normalizedCurrent)) {
                score += 3000;
            }

            if (normalizedCurrent.contains(normalizedDbQuestion) && normalizedDbQuestion.length() >= 2) {
                score += 2500;
            }
        }

        if (!normalizedRewritten.isEmpty()) {
            if (normalizedDbQuestion.contains(normalizedRewritten)) {
                score += 2500;
            }

            if (normalizedRewritten.contains(normalizedDbQuestion) && normalizedDbQuestion.length() >= 2) {
                score += 2000;
            }
        }

        // 3. 检索词命中位置评分
        for (String query : queries) {
            String q = normalizeText(query);

            if (q.isEmpty()) {
                continue;
            }

            if (normalizedDbQuestion.equals(q)) {
                score += 1800;
            }

            if (normalizedDbQuestion.contains(q)) {
                score += 800;
            }

            if (normalizeText(dbKeywords).contains(q)) {
                score += 500;
            }

            if (normalizeText(dbAnswer).contains(q)) {
                score += 120;
            }
        }

        // 4. 中文片段重合度评分
        List<String> fragments = buildFragments(normalizedCurrent + normalizedRewritten);

        for (String fragment : fragments) {
            if (fragment.length() < 2) {
                continue;
            }

            if (normalizedDbQuestion.contains(fragment)) {
                score += 120;
            }

            if (normalizeText(dbKeywords).contains(fragment)) {
                score += 80;
            }

            if (normalizeText(dbAnswer).contains(fragment)) {
                score += 20;
            }
        }

        // 5. 浏览量作为轻微加权，不能压过相似度
        if (knowledge.getViewCount() != null) {
            score += Math.min(knowledge.getViewCount(), 100);
        }

        return score;
    }

    /**
     * 把命中的知识库内容组装给 Ollama。
     */
    private String buildKnowledgeText(List<Knowledge> knowledgeList) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < knowledgeList.size(); i++) {
            Knowledge k = knowledgeList.get(i);

            sb.append("【数据库命中资料 ")
                    .append(i + 1)
                    .append("】\n");

            sb.append("问题：")
                    .append(safe(k.getQuestion()))
                    .append("\n");

            sb.append("答案：")
                    .append(safe(k.getAnswer()))
                    .append("\n");

            if (k.getKeywords() != null && !k.getKeywords().isBlank()) {
                sb.append("关键词：")
                        .append(k.getKeywords())
                        .append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 数据库命中后，基于知识库自然回答。
     */
    private String answerWithKnowledge(
            String fullQuestion,
            String currentQuestion,
            String rewrittenQuestion,
            String knowledgeText
    ) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content",
                                    "你是华南理工大学校园生活百事通智能助手。" +
                                            "系统已经从数据库中找到与用户问题相同或类似的资料。" +
                                            "你必须以数据库命中的资料为事实依据回答，不能脱离资料自由发挥。" +
                                            "如果数据库资料只有一句简短答案，也必须围绕这句答案整理，不能扩展成其他泛泛信息。" +
                                            "如果数据库答案表示不确定，例如“具体情况可能调整”“建议现场查看”，你必须保留这种不确定性。" +
                                            "不要编造数据库中没有的具体时间、地点、政策、流程。" +
                                            "不要说“根据数据库资料”这种生硬表达。" +
                                            "请把数据库答案整理得自然、简洁、像客服回答。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content",
                                    "用户原始上下文和问题：\n" + fullQuestion +
                                            "\n\n用户当前真正的问题：\n" + currentQuestion +
                                            "\n\n根据上下文改写后的检索问题：\n" + rewrittenQuestion +
                                            "\n\n数据库命中的资料如下：\n" + knowledgeText +
                                            "\n\n请严格基于数据库资料，回答用户当前问题。"
                            )
                    ),
                    "stream", false,
                    "temperature", 0.2
            );

            return callOllama(requestBody);

        } catch (Exception e) {
            return "抱歉，知识库中找到了相关信息，但生成回答失败。";
        }
    }

    /**
     * 数据库没命中时，正常调用 Ollama。
     */
    private String askOllamaNormally(String question) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content",
                                    "你是校园生活百事通智能助手。请用中文自然、简洁、友好地回答用户问题。" +
                                            "如果用户表达的是隐含需求，也要尽量理解其真实意图。" +
                                            "例如用户说“我饿了”，通常是在表达想找吃饭地点或食堂信息。" +
                                            "如果你不确定，不要编造具体事实，可以建议用户查看学校官网或咨询相关部门。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", question
                            )
                    ),
                    "stream", false,
                    "temperature", 0.5
            );

            return callOllama(requestBody);

        } catch (Exception e) {
            return "抱歉，知识库暂无相关信息。";
        }
    }

    /**
     * 统一调用 Ollama，并解析 OpenAI 兼容格式返回。
     */
    private String callOllama(Map<String, Object> requestBody) {
        Map response = restTemplate.postForObject(aiUrl, requestBody, Map.class);

        if (response == null) {
            return "AI模型没有返回内容，请检查 Ollama 是否启动。";
        }

        List choices = (List) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "AI模型返回格式异常，请检查模型名称是否正确。";
        }

        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        if (message == null) {
            return "AI模型返回内容为空。";
        }

        Object content = message.get("content");

        if (content == null) {
            return "AI没有生成有效回答。";
        }

        return cleanAnswer(content.toString());
    }

    /**
     * 判断用户是不是在问系统状态、知识库数量、自检信息。
     */
    private boolean isSystemCheckQuestion(String question) {
        if (question == null) {
            return false;
        }

        String q = question.toLowerCase();

        return q.contains("自检")
                || q.contains("检查系统")
                || q.contains("系统状态")
                || q.contains("运行状态")
                || q.contains("知识库多少")
                || q.contains("知识库有多少")
                || q.contains("多少条数据")
                || q.contains("数据库多少")
                || q.contains("数据量")
                || q.contains("你能调用的数据库")
                || q.contains("你能访问多少");
    }

    /**
     * 真实系统自检。
     * 这里不让 AI 编，直接查数据库和 Ollama 状态。
     */
    private String buildSystemCheckAnswer() {
        StringBuilder sb = new StringBuilder();

        sb.append("系统自检结果如下：\n\n");

        try {
            int total = knowledgeMapper.countAll();
            int enabled = knowledgeMapper.countEnabled();
            int disabled = knowledgeMapper.countDisabled();
            int categoryCount = knowledgeMapper.countUsedCategory();
            String latestUpdateTime = knowledgeMapper.latestUpdateTime();

            sb.append("1. 知识库状态\n");
            sb.append("- 知识库总数据：").append(total).append(" 条\n");
            sb.append("- 启用数据：").append(enabled).append(" 条\n");
            sb.append("- 停用数据：").append(disabled).append(" 条\n");
            sb.append("- 涉及分类数量：").append(categoryCount).append(" 个\n");
            sb.append("- 最近更新时间：").append(latestUpdateTime == null ? "暂无" : latestUpdateTime).append("\n\n");

            if (total <= 0) {
                sb.append("知识库异常：当前没有可用数据。\n\n");
            } else if (enabled <= 0) {
                sb.append("知识库异常：虽然有数据，但没有启用数据。\n\n");
            } else {
                sb.append("知识库检查：正常。\n\n");
            }

        } catch (Exception e) {
            sb.append("1. 知识库状态\n");
            sb.append("- 检查失败：无法读取 knowledge 表。\n\n");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of("role", "user", "content", "请只回复 OK")
                    ),
                    "stream", false
            );

            String result = callOllama(requestBody);

            if (result != null && !result.trim().isEmpty()) {
                sb.append("2. 本地 AI 模型状态\n");
                sb.append("- Ollama 连接：正常\n");
                sb.append("- 当前模型：").append(aiModel).append("\n\n");
            } else {
                sb.append("2. 本地 AI 模型状态\n");
                sb.append("- Ollama 连接：异常，模型没有返回内容。\n\n");
            }

        } catch (Exception e) {
            sb.append("2. 本地 AI 模型状态\n");
            sb.append("- Ollama 连接：异常，请检查 Ollama 是否启动。\n\n");
        }

        sb.append("3. 综合判断\n");
        sb.append("- 后端服务：当前接口可正常响应\n");
        sb.append("- 数据库连接：如果上方能显示知识库数量，则数据库连接正常\n");
        sb.append("- 知识库问答：会优先检索 knowledge 表，再把数据库答案交给 Ollama 进行自然化回答\n");

        return sb.toString();
    }

    /**
     * 清理 AI 回答开头的废话。
     */
    private String cleanAnswer(String answer) {
        if (answer == null) {
            return "";
        }

        String result = answer.trim();

        String[] badPrefixes = {
                "明白了，",
                "明白了。",
                "好的，",
                "好的。",
                "当然，",
                "当然。",
                "可以，",
                "可以。",
                "没问题，",
                "没问题。"
        };

        boolean changed = true;

        while (changed) {
            changed = false;

            for (String prefix : badPrefixes) {
                if (result.startsWith(prefix)) {
                    result = result.substring(prefix.length()).trim();
                    changed = true;
                }
            }
        }

        return result;
    }

    private void addQuery(List<String> queries, String query) {
        if (query == null) {
            return;
        }

        String q = query.trim();

        if (q.isEmpty()) {
            return;
        }

        if (!queries.contains(q)) {
            queries.add(q);
        }
    }

    private boolean containsAny(String text, List<String> keywords) {
        if (text == null || keywords == null) {
            return false;
        }

        for (String keyword : keywords) {
            if (keyword != null && text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[\\s\\p{Punct}，。！？、；：,.!?;:（）()【】\\[\\]《》<>“”\"'‘’]", "")
                .replace("请问", "")
                .replace("一下", "")
                .replace("可以", "")
                .replace("能不能", "")
                .replace("能否", "")
                .replace("是否", "")
                .replace("什么", "")
                .replace("怎么", "")
                .replace("如何", "")
                .replace("吗", "")
                .replace("呢", "")
                .trim();
    }

    private void addQuestionFragments(List<String> queries, String question) {
        String text = normalizeText(question);

        if (text.length() < 2) {
            return;
        }

        for (int len = 6; len >= 2; len--) {
            for (int i = 0; i + len <= text.length(); i++) {
                String part = text.substring(i, i + len);
                addQuery(queries, part);

                if (queries.size() >= 30) {
                    return;
                }
            }
        }
    }

    private List<String> buildFragments(String text) {
        List<String> fragments = new ArrayList<>();

        if (text == null || text.length() < 2) {
            return fragments;
        }

        String normalized = normalizeText(text);

        for (int len = 4; len >= 2; len--) {
            for (int i = 0; i + len <= normalized.length(); i++) {
                String part = normalized.substring(i, i + len);

                if (!fragments.contains(part)) {
                    fragments.add(part);
                }

                if (fragments.size() >= 20) {
                    return fragments;
                }
            }
        }

        return fragments;
    }

    private static class KnowledgeScore {
        private final Knowledge knowledge;
        private final int score;

        private KnowledgeScore(Knowledge knowledge, int score) {
            this.knowledge = knowledge;
            this.score = score;
        }
    }
}