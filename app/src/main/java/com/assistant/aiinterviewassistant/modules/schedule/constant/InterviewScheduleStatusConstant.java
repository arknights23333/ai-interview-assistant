package com.assistant.aiinterviewassistant.modules.schedule.constant;

import java.util.Set;

/** 面试日程状态，对应 interview_schedule.status。 */
public final class InterviewScheduleStatusConstant {

    /** 待进行，新建日程的默认状态。 */
    public static final String PENDING = "PENDING";

    /** 已完成。 */
    public static final String COMPLETED = "COMPLETED";

    /** 已取消。 */
    public static final String CANCELLED = "CANCELLED";

    /** 日程状态更新和列表筛选允许使用的状态。 */
    public static final Set<String> VALID_STATUSES = Set.of(PENDING, COMPLETED, CANCELLED);

    private InterviewScheduleStatusConstant() {}
}
