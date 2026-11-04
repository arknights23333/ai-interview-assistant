package com.assistant.aiinterviewassistant.modules.resume.service.impl;

import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.assistant.aiinterviewassistant.modules.resume.service.ResumeService;
import com.assistant.aiinterviewassistant.modules.resume.mapper.ResumeMapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, ResumePo> implements ResumeService {
}
