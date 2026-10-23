# 智能面试辅助系统模型选型

确定日期：2026-10-23

本文记录第一版系统的模型选型与切换规则，作为后续开发依据。这里列出的是计划接入的模型与服务，不表示已经完成接入或验证。

## 一、选型总览

| 能力 | 第一版选择 | 切换规则 |
| --- | --- | --- |
| LLM 聊天模型 | 通义千问、DeepSeek、智谱 GLM、Kimi | 支持配置和选择多个模型 |
| Embedding 向量模型 | 百炼 `qwen3.7-text-embedding`，固定 1024 维 | 不提供模型或维度切换入口 |
| ASR 语音识别 | 阿里云百炼、腾讯云，各接入一套 | 开始面试前选择，本场固定 |
| TTS 语音合成 | 阿里云百炼、腾讯云，各接入一套 | 开始面试前选择模型及音色，本场固定 |

## 二、LLM 聊天模型

计划接入四家厂商，先准备以下五个模型选项：

| 厂商 | 模型名称或 ID | 计划用途 |
| --- | --- | --- |
| 阿里云百炼 | `qwen3.8-flash` | 默认候选，用于日常调试和面试对话 |
| 阿里云百炼 | `qwen3.7-plus` | 用于比较出题、简历分析和面试评估效果 |
| DeepSeek | `deepseek-flash` | 优先接入的第二家服务，验证跨厂商切换 |
| 智谱 | GLM-5.3-Flash | 补充国产聊天模型选择 |
| 月之暗面 | Kimi K2.6 | 补充对话与分析模型选择 |

说明：

- 优先接通千问和 DeepSeek，再接入 GLM、Kimi。
- GLM、Kimi 的最终 API model 字符串在接入时按对应官方平台核对，不能仅由展示名称推断。
- 服务地址、API Key、模型名称集中配置；兼容接口下允许填写自定义模型名称。
- OpenAI 兼容接口可以复用调用逻辑，但流式输出、结构化输出、思考模式和参数支持仍需分别验证。
- 本表是接入计划，不代表已经完成模型效果排名；使用相同样本比较后再确定业务默认值。
- 聊天模型与 Embedding 模型独立配置，切换聊天模型不需要重新生成知识库向量。

## 三、Embedding 向量模型

最终选择：**阿里云百炼 `qwen3.7-text-embedding`，显式设置 `dimensions=1024`。**

此前讨论的 `text-embedding-v4` 不再作为第一版最终选型。

固定约定：

- 向量类型：稠密向量。
- 输出维度：1024。
- 数据库存储：PostgreSQL + pgvector，向量字段采用 `vector(1024)`。
- 相似度计算：余弦距离（COSINE）。
- 文档入库和用户问题向量化使用同一个模型及一致的维度配置。
- 第一版不实现 Embedding 模型切换、多模型向量索引或跨维度迁移。
- 更长的输入上限不替代文档切分，知识库仍需合理分块。

原因：不同 Embedding 模型即使输出维度相同，向量空间也不一定兼容。若将来更换模型，需要重新生成受影响文档的向量，或者另行维护对应模型的独立向量索引。

## 四、ASR 语音识别

| 厂商 | 模型或服务 | 第一版约定 |
| --- | --- | --- |
| 阿里云百炼 | `qwen3-asr-flash-realtime` | 第一套实现，对照参考项目跑通实时识别 |
| 腾讯云 | 实时语音识别 WebSocket，`16k_zh` 中文通用引擎 | 第二套实现，验证跨厂商选择 |

优先以 16kHz、16 位、单声道 PCM 作为系统录音传输约定；接入时核对各服务的实际要求。

## 五、TTS 语音合成

| 厂商 | 模型或服务 | 第一版约定 |
| --- | --- | --- |
| 阿里云百炼 | `qwen3-tts-flash-realtime` | 第一套实现，对照参考项目跑通合成与播放 |
| 腾讯云 | 流式文本语音合成 WebSocket（`TextToStreamAudioWSv2`） | 第二套实现，通过 `VoiceType` 选择音色 |

说明：

- 各厂商的默认音色在开通后试听确定，目前尚未敲定。
- 腾讯云按服务接口和音色选择，不强行套用百炼的 model 参数形式。
- 百炼参考实现使用 24kHz PCM；腾讯云部分音色才支持 24kHz，接入时确认所选音色的采样率。
- 第一版使用系统音色，不做声音复刻和声音设计。

## 六、语音模型切换规则

- 系统设置维护默认 ASR、TTS 和音色。
- 创建语音面试时允许为本场选择 ASR、TTS 和音色，两者可以独立组合。
- 一场面试指同一条面试会话记录，不是一次 WebSocket 连接。
- 面试开始后保持本场配置；暂停、恢复和断线重连继续使用同一配置。
- 修改系统默认配置只影响后续新建面试，不改变正在进行的面试。
- 创建会话时保存模型配置标识及必要的版本或参数快照；API Key 单独管理，不复制到面试记录中。
- 第一版不做会话中途切换或故障时自动切换供应商。

## 七、接入顺序

1. 跑通千问聊天模型和固定 Embedding 模型。
2. 接入 DeepSeek，验证聊天模型切换。
3. 跑通百炼 ASR → LLM → 百炼 TTS 的语音面试链路。
4. 增加腾讯云 ASR、TTS，验证按面试场次选择和独立组合。
5. 补充 GLM、Kimi，并验证实际所需的流式与结构化输出能力。

## 八、官方参考资料

以下链接用于核对型号、接口与能力；实际开通权限、价格、免费额度和模型可用性以接入时的官方平台为准。

- [百炼文本生成模型](https://help.aliyun.com/zh/model-studio/text-generation-model/)
- [DeepSeek API 接入](https://api-docs.deepseek.com/)
- [智谱模型概览](https://docs.bigmodel.cn/cn/guide/start/model-overview)
- [Kimi 开放平台](https://platform.kimi.com/)
- [百炼 Embedding 同步接口](https://help.aliyun.com/zh/model-studio/text-embedding-synchronous-api)
- [百炼语音识别模型](https://help.aliyun.com/zh/model-studio/asr-model/)
- [百炼实时 TTS Java SDK](https://help.aliyun.com/zh/model-studio/qwen-tts-realtime-java-sdk)
- [腾讯云实时语音识别](https://cloud.tencent.com/document/product/1093/48982)
- [腾讯云流式文本语音合成](https://cloud.tencent.com/document/api/1073/108595)
