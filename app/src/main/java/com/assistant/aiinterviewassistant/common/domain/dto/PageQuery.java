package com.assistant.aiinterviewassistant.common.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 通用分页查询参数，通过 URL 查询参数接收。 */
@Data
public class PageQuery {

    /** 页码，从 1 开始，默认第一页。 */
    @Min(value = 1, message = "页码不能小于1")
    private long pageNum = 1;

    /** 每页条数，默认 10 条，最多 100 条。 */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long pageSize = 10;
}
