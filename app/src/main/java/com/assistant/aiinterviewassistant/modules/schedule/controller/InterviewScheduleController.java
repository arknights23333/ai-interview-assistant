package com.assistant.aiinterviewassistant.modules.schedule.controller;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleCreateDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleQueryDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.vo.ScheduleVo;
import com.assistant.aiinterviewassistant.modules.schedule.service.InterviewScheduleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试日程管理模块
 * @folder 面试日程管理模块
 */
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class InterviewScheduleController {

    private final InterviewScheduleService interviewScheduleService;

    /**
     * 创建面试日程
     * @param jwt
     * @param dto
     * @return
     */
    @PostMapping("/create")
    public R<String> createSchedule(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody ScheduleCreateDto dto){
        String id = interviewScheduleService.createSchedule(Long.valueOf(jwt.getSubject()),dto);

        return R.ok(id,"创建成功");
    }

    /**
     * 修改面试日程
     * @param jwt
     * @param dto
     * @return
     */
    @PutMapping("/update/{id}")
    public R<Void> updateSchedule (@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody ScheduleCreateDto dto,
                                   @PathVariable Long id){
        interviewScheduleService.updateSchedule(Long.valueOf(jwt.getSubject()),dto,id);
        return R.ok("修改成功");
    }

    /**
     * 获取面试日程列表
     * @param jwt
     * @param dto 查询参数 时间范围，公司名字，状态。
     * @param pageQuery 分页参数
     * @return
     */
    @PostMapping("/list")
    public R<Page<ScheduleVo>> getScheduleList(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody ScheduleQueryDto dto,
                                               @Valid @ModelAttribute PageQuery pageQuery){
        return interviewScheduleService.getScheduleList(Long.valueOf(jwt.getSubject()),dto,pageQuery);
    }
    /**
     * 删除面试日程
     * @param jwt
     * @param ids 删除的面试日程ID列表
     * @return
     */
    @DeleteMapping("/delete")
    public R<List<Long>> deleteSchedule(@AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody List<Long> ids){
        return interviewScheduleService.deleteSchedule(Long.valueOf(jwt.getSubject()),ids);

    }

}
