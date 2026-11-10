package com.assistant.aiinterviewassistant.modules.knowledgebase.mapper;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface DocumentMapper  extends BaseMapper<DocumentPo> {
}
