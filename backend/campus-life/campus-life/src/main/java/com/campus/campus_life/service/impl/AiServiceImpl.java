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

    @Value("${ai.debug:false}")
    private boolean aiDebug;

    @Value("${ai.min-score:1000}")
    private int aiMinScore;

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private KnowledgeMapper knowledgeMapper;

    @Override
    public String chat(String question) {
        try {
            if (question == null || question.trim().isEmpty()) {
                return "请先输入你的问题。";
            }

            String currentQuestion = extractCurrentQuestion(question);

            // 0. 打招呼、闲聊、无明确校园业务问题时，不进入知识库检索
            if (isGreetingOrSmallTalk(currentQuestion)) {
                return "你好，我是 BBG 校园生活百事通。你可以问我食堂餐饮、校园卡、宿舍报修、图书馆、教务通知、校园生活等问题。";
            }

            if (isSystemCheckQuestion(currentQuestion)) {
                return buildSystemCheckAnswer();
            }

            if (isPersonalAcademicInfoQuestion(currentQuestion)) {
                return "目前系统还没有同步你的个人教务信息，所以暂时不能直接查询你的个人课表、成绩、绩点或考试安排。后续可以在“我的教务信息”功能中同步个人数据后，再为你查询具体信息。";
            }

            String rewrittenQuestion = rewriteQuestionByContext(question, currentQuestion);

            List<String> searchQueries = buildSearchQueries(
                    question,
                    currentQuestion,
                    rewrittenQuestion
            );

            List<Knowledge> knowledgeList = searchKnowledgeByQueries(
                    searchQueries,
                    currentQuestion,
                    rewrittenQuestion
            );

            if (aiDebug) {
                printAiSearchDebug(
                        question,
                        currentQuestion,
                        rewrittenQuestion,
                        searchQueries,
                        knowledgeList
                );
            }

            if (knowledgeList != null && !knowledgeList.isEmpty()) {
                int bestScore = calculateKnowledgeScore(
                        knowledgeList.get(0),
                        currentQuestion,
                        rewrittenQuestion,
                        searchQueries
                );

                if (bestScore >= aiMinScore) {
                    String knowledgeText = buildKnowledgeText(knowledgeList);
                    return answerWithKnowledge(question, currentQuestion, rewrittenQuestion, knowledgeText);
                }

                if (aiDebug) {
                    System.out.println("知识库最高相似度低于阈值，转为谨慎普通回答。");
                    System.out.println("最高相似度：" + bestScore);
                    System.out.println("阈值：" + aiMinScore);
                }
            }

            return askOllamaNormally(question);

        } catch (Exception e) {
            e.printStackTrace();
            return "AI模型调用失败，请检查 Ollama 是否已启动，以及模型名称是否正确。";
        }
    }

    private void printAiSearchDebug(
            String originalQuestion,
            String currentQuestion,
            String rewrittenQuestion,
            List<String> searchQueries,
            List<Knowledge> knowledgeList
    ) {
        System.out.println();
        System.out.println("========== AI 检索调试 ==========");
        System.out.println("用户原始问题：");
        System.out.println(originalQuestion);
        System.out.println("--------------------------------");
        System.out.println("当前真实问题：" + currentQuestion);
        System.out.println("改写后的问题：" + rewrittenQuestion);
        System.out.println("检索词列表：" + searchQueries);

        if (knowledgeList == null || knowledgeList.isEmpty()) {
            System.out.println("知识库命中结果：0 条");
        } else {
            System.out.println("知识库命中结果：" + knowledgeList.size() + " 条");

            for (int i = 0; i < knowledgeList.size(); i++) {
                Knowledge k = knowledgeList.get(i);
                int score = calculateKnowledgeScore(k, currentQuestion, rewrittenQuestion, searchQueries);

                System.out.println("---- 命中资料 " + (i + 1) + " ----");
                System.out.println("相似度分数：" + score);
                System.out.println("ID：" + k.getId());
                System.out.println("问题：" + k.getQuestion());
                System.out.println("答案：" + k.getAnswer());
                System.out.println("关键词：" + k.getKeywords());
            }
        }

        System.out.println("================================");
        System.out.println();
    }

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
                                            "只输出改写后的问题，不要解释，不要回答。" +

                                            "改写规则：" +
                                            "1. 不要以“明白了”“好的”“当然”“可以”“没问题”等客套话开头。" +
                                            "2. 如果用户说“我饿了”“想吃饭”“肚子饿”“吃什么”，应理解为泛化咨询食堂、餐饮、饭堂、吃饭地点、营业时间等信息。" +
                                            "不要在用户没有明确提到具体食堂名称时，擅自改写成某个具体食堂，例如不要擅自改写成“一饭”“二饭”“三饭”。" +
                                            "3. 如果用户说“不舒服”“生病了”，应理解为咨询校医院、医疗、就医等信息。" +
                                            "4. 如果用户说“卡丢了”“饭卡没了”，应理解为咨询校园卡挂失、补办等信息。" +
                                            "5. 如果用户说“宿舍坏了”“灯坏了”“空调坏了”，应理解为咨询宿舍报修。" +
                                            "6. 如果用户询问“这学期我的课表”“我的成绩”“我的绩点”“我的考试”，应保留其个人信息查询含义。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content",
                                    "完整上下文：\n" + fullQuestion +
                                            "\n\n用户当前问题：\n" + currentQuestion +
                                            "\n\n请输出适合检索校园知识库的独立问题："
                            )
                    ),
                    "stream", false,
                    "temperature", 0.1
            );

            String result = callOllama(requestBody);

            if (result == null || result.trim().isEmpty()) {
                return currentQuestion;
            }

            return result.trim()
                    .replace("改写后的问题：", "")
                    .replace("独立问题：", "")
                    .replace("检索问题：", "")
                    .replace("\"", "")
                    .replace("“", "")
                    .replace("”", "")
                    .trim();

        } catch (Exception e) {
            return currentQuestion;
        }
    }

    private List<String> buildSearchQueries(String fullQuestion, String currentQuestion, String rewrittenQuestion) {
        List<String> queries = new ArrayList<>();

        addQuery(queries, currentQuestion);
        addQuery(queries, rewrittenQuestion);

        String focusText = (
                (currentQuestion == null ? "" : currentQuestion) + " " +
                        (rewrittenQuestion == null ? "" : rewrittenQuestion)
        ).toLowerCase();

        if (containsAny(focusText, List.of("饿", "吃饭", "吃东西", "饭", "食堂", "饭堂", "餐厅", "餐饮", "早餐", "午餐", "晚餐", "夜宵", "菜", "食物", "美食", "堂食", "二饭", "一饭"))) {

            if (focusText.contains("二饭") || focusText.contains("第二饭堂")) {
                addQuery(queries, "二饭可以堂食吗");
                addQuery(queries, "二饭堂食");
                addQuery(queries, "二饭");
            }

            if (focusText.contains("堂食")) {
                addQuery(queries, "堂食");
                addQuery(queries, "食堂堂食");
                addQuery(queries, "食堂是否可以堂食");
                addQuery(queries, "哪个食堂可以堂食");
            }

            if (focusText.contains("一饭") || focusText.contains("第一饭堂")) {
                addQuery(queries, "一饭");
                addQuery(queries, "第一饭堂");
            }

            addQuery(queries, "食堂");
            addQuery(queries, "餐饮");
            addQuery(queries, "吃饭");
            addQuery(queries, "饭堂");
            addQuery(queries, "食堂开放时间");
            addQuery(queries, "食堂位置");
            addQuery(queries, "有哪些食堂");
            addQuery(queries, "哪里可以吃饭");
        }

        if (containsAny(focusText, List.of("不舒服", "生病", "发烧", "感冒", "头疼", "肚子疼", "受伤", "校医院", "看病", "就医", "医生"))) {
            addQuery(queries, "校医院");
            addQuery(queries, "医疗");
            addQuery(queries, "就医");
            addQuery(queries, "看病");
        }

        if (containsAny(focusText, List.of("校园卡", "饭卡", "卡丢", "卡没了", "挂失", "补办", "一卡通", "充值"))) {
            addQuery(queries, "校园卡");
            addQuery(queries, "挂失");
            addQuery(queries, "补办");
            addQuery(queries, "充值");
        }

        if (containsAny(focusText, List.of("宿舍", "坏了", "报修", "空调", "水电", "灯坏", "门坏", "维修"))) {
            addQuery(queries, "宿舍报修");
            addQuery(queries, "报修");
            addQuery(queries, "后勤");
        }

        if (containsAny(focusText, List.of("图书馆", "借书", "还书", "续借", "自习", "座位", "文献"))) {
            addQuery(queries, "图书馆");
            addQuery(queries, "借书");
            addQuery(queries, "续借");
            addQuery(queries, "自习");
        }

        if (containsAny(focusText, List.of("选课", "成绩", "考试", "课表", "教务", "补考", "缓考", "学分", "注册", "开学", "放假"))) {
            addQuery(queries, "教务");
            addQuery(queries, "选课");
            addQuery(queries, "考试");
            addQuery(queries, "成绩");
            addQuery(queries, "课表");
        }

        addQuestionFragments(queries, currentQuestion);
        addQuestionFragments(queries, rewrittenQuestion);

        return queries;
    }

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

        if (isGenericFoodQuestion(currentQuestion, rewrittenQuestion)) {
            List<Knowledge> filtered = candidates.stream()
                    .filter(this::isGeneralFoodKnowledge)
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                candidates = filtered;
            }
        } else if (!hasExplicitCanteenName(currentQuestion)) {
            List<Knowledge> filtered = candidates.stream()
                    .filter(k -> !isSpecificCanteenKnowledge(k))
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                candidates = filtered;
            }
        }

        List<KnowledgeScore> scoredList = candidates.stream()
                .map(k -> new KnowledgeScore(k, calculateKnowledgeScore(k, currentQuestion, rewrittenQuestion, queries)))
                .filter(item -> item.score > 0)
                .sorted(Comparator.comparingInt((KnowledgeScore item) -> item.score).reversed())
                .collect(Collectors.toList());

        if (scoredList.isEmpty()) {
            return new ArrayList<>();
        }

        KnowledgeScore best = scoredList.get(0);

        if (best.score >= 10000) {
            List<Knowledge> exactOnly = new ArrayList<>();
            exactOnly.add(best.knowledge);
            return exactOnly;
        }

        List<Knowledge> result = new ArrayList<>();

        for (KnowledgeScore item : scoredList) {
            if (result.size() >= 5) {
                break;
            }

            result.add(item.knowledge);
        }

        return result;
    }

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

        if (!normalizedCurrent.isEmpty() && normalizedDbQuestion.equals(normalizedCurrent)) {
            score += 10000;
        }

        if (!normalizedRewritten.isEmpty() && normalizedDbQuestion.equals(normalizedRewritten)) {
            score += 9500;
        }

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

        if (knowledge.getViewCount() != null) {
            score += Math.min(knowledge.getViewCount(), 100);
        }

        return score;
    }

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
                                    "你是华南理工大学校园生活百事通智能助手，回答风格要像学校事务客服，简洁、准确、自然。" +
                                            "现在系统已经从数据库中检索到了与用户问题相同或相似的资料。" +

                                            "你必须严格遵守以下规则：" +
                                            "1. 必须以数据库命中的资料为唯一事实依据回答。" +
                                            "2. 不允许脱离数据库资料编造具体时间、地点、流程、政策、电话、网址。" +
                                            "3. 如果数据库资料只有一句简短答案，也只能围绕这句答案自然整理，不能扩展到无关内容。" +
                                            "4. 如果数据库答案表示不确定，例如“可能调整”“建议现场查看”“以通知为准”，必须保留这种不确定性。" +
                                            "5. 不要说“根据数据库资料”“资料显示”“知识库显示”这种生硬表达。" +
                                            "6. 不要以“好的”“当然”“明白了”“没问题”开头。" +
                                            "7. 如果数据库资料无法完整回答用户问题，要明确说明目前信息有限，并建议用户查看现场通知、官网或咨询相关部门。" +
                                            "8. 回答必须优先直接回应用户的问题，不要泛泛介绍整个学校或无关内容。" +
                                            "9. 如果用户问的是某个具体对象，例如“二饭可以堂食吗”，就只回答这个具体对象，不要扩展成所有食堂介绍。" +
                                            "10. 如果数据库答案本身很短，不要为了显得丰富而添加数据库没有的事实。" +
                                            "11. 如果用户只是说“我饿了”“想吃饭”等泛化需求，且没有说明校区或当前位置，可以先给出通用食堂信息，再提醒用户说明所在校区或位置，以便进一步推荐。" +

                                            "输出要求：" +
                                            "1. 使用中文。" +
                                            "2. 尽量控制在 1 到 3 段。" +
                                            "3. 如果适合，可以用简短列表。" +
                                            "4. 语气友好，但不要啰嗦。"
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
                    "temperature", 0.15
            );

            return callOllama(requestBody);

        } catch (Exception e) {
            return "抱歉，知识库中找到了相关信息，但生成回答失败。";
        }
    }

    private String askOllamaNormally(String question) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content",
                                    "你是华南理工大学校园生活百事通智能助手，回答风格要像校园事务客服。" +
                                            "当前系统没有从知识库中检索到可靠资料，因此你只能进行谨慎回答。" +

                                            "你必须遵守以下规则：" +
                                            "1. 不确定的问题不要编造具体事实。" +
                                            "2. 不要编造学校政策、时间、地点、电话、网址、办理流程。" +
                                            "3. 如果无法确定，应建议用户查看学校官网、相关部门通知，或咨询学院、辅导员、教务员、相关部门。" +
                                            "4. 不要以“好的”“当然”“明白了”“没问题”开头。" +
                                            "5. 用户表达比较口语时，要理解真实意图。" +
                                            "例如：用户说“我饿了”，通常是在问食堂或餐饮；" +
                                            "用户说“不舒服”，通常是在问校医院或就医；" +
                                            "用户说“卡丢了”，通常是在问校园卡挂失或补办；" +
                                            "用户说“宿舍坏了”，通常是在问宿舍报修。" +
                                            "6. 如果用户询问个人课表、成绩、绩点、考试安排等个人信息，而系统没有同步个人数据，应明确说明暂未同步个人信息。" +

                                            "输出要求：" +
                                            "1. 使用中文。" +
                                            "2. 回答简洁自然。" +
                                            "3. 不要长篇泛泛而谈。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", question
                            )
                    ),
                    "stream", false,
                    "temperature", 0.35
            );

            return callOllama(requestBody);

        } catch (Exception e) {
            return "抱歉，知识库暂无相关信息。";
        }
    }

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

    private boolean isGreetingOrSmallTalk(String question) {
        if (question == null) {
            return false;
        }

        String q = normalizeText(question);

        if (q.equals("你好")
                || q.equals("您好")
                || q.equals("hello")
                || q.equals("hi")
                || q.equals("hey")
                || q.equals("hallo")
                || q.equals("哈喽")
                || q.equals("嗨")
                || q.equals("在吗")
                || q.equals("在不在")) {
            return true;
        }

        if (q.equals("ok")
                || q.equals("okay")
                || q.equals("好")
                || q.equals("好的")
                || q.equals("嗯")
                || q.equals("嗯嗯")
                || q.equals("谢谢")
                || q.equals("感谢")) {
            return true;
        }

        if (q.length() <= 2
                && !q.contains("饭")
                && !q.contains("卡")
                && !q.contains("课")
                && !q.contains("考")
                && !q.contains("宿舍")
                && !q.contains("图书馆")
                && !q.contains("食堂")
                && !q.contains("校医院")
                && !q.contains("报修")) {
            return true;
        }

        return false;
    }

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

    private boolean isPersonalAcademicInfoQuestion(String question) {
        if (question == null) {
            return false;
        }

        String q = normalizeText(question);

        boolean hasPersonalWord =
                q.contains("我")
                        || q.contains("我的")
                        || q.contains("本人")
                        || q.contains("自己");

        boolean hasAcademicPrivateWord =
                q.contains("课表")
                        || q.contains("课程表")
                        || q.contains("成绩")
                        || q.contains("绩点")
                        || q.contains("gpa")
                        || q.contains("考试安排")
                        || q.contains("明天考试")
                        || q.contains("有什么课")
                        || q.contains("我的课")
                        || q.contains("这学期课");

        return hasPersonalWord && hasAcademicPrivateWord;
    }

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

    private boolean isGenericFoodQuestion(String currentQuestion, String rewrittenQuestion) {
        String text = normalizeText(
                safe(currentQuestion) + " " + safe(rewrittenQuestion)
        );

        boolean hasFoodIntent =
                text.contains("饿")
                        || text.contains("吃饭")
                        || text.contains("吃东西")
                        || text.contains("食堂")
                        || text.contains("饭堂")
                        || text.contains("餐饮")
                        || text.contains("吃什么")
                        || text.contains("就餐");

        boolean hasSpecificTarget =
                text.contains("一饭")
                        || text.contains("二饭")
                        || text.contains("三饭")
                        || text.contains("第一饭堂")
                        || text.contains("第二饭堂")
                        || text.contains("第三饭堂")
                        || text.contains("堂食")
                        || text.contains("夜宵")
                        || text.contains("烧腊")
                        || text.contains("饮料");

        return hasFoodIntent && !hasSpecificTarget;
    }

    private boolean isGeneralFoodKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            return false;
        }

        String text = normalizeText(
                safe(knowledge.getQuestion()) + " " +
                        safe(knowledge.getAnswer()) + " " +
                        safe(knowledge.getKeywords())
        );

        boolean isFoodRelated =
                text.contains("食堂")
                        || text.contains("饭堂")
                        || text.contains("餐饮")
                        || text.contains("吃饭")
                        || text.contains("就餐");

        boolean isGeneralInfo =
                text.contains("开放时间")
                        || text.contains("营业时间")
                        || text.contains("位置")
                        || text.contains("在哪")
                        || text.contains("哪里")
                        || text.contains("有哪些")
                        || text.contains("就餐")
                        || text.contains("吃饭地点");

        boolean tooSpecific =
                text.contains("一饭")
                        || text.contains("二饭")
                        || text.contains("三饭")
                        || text.contains("北区")
                        || text.contains("烧腊")
                        || text.contains("饮料")
                        || text.contains("夜宵")
                        || text.contains("供应吗")
                        || text.contains("有吗")
                        || text.contains("停止营业");

        return isFoodRelated && isGeneralInfo && !tooSpecific;
    }

    private boolean hasExplicitCanteenName(String question) {
        if (question == null) {
            return false;
        }

        String q = normalizeText(question);

        return q.contains("一饭")
                || q.contains("二饭")
                || q.contains("三饭")
                || q.contains("第一饭堂")
                || q.contains("第二饭堂")
                || q.contains("第三饭堂");
    }

    private boolean isSpecificCanteenKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            return false;
        }

        String text = normalizeText(
                safe(knowledge.getQuestion()) + " " +
                        safe(knowledge.getKeywords())
        );

        return text.contains("一饭")
                || text.contains("二饭")
                || text.contains("三饭")
                || text.contains("第一饭堂")
                || text.contains("第二饭堂")
                || text.contains("第三饭堂");
    }

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
                "没问题。",
                "您好，",
                "您好。"
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