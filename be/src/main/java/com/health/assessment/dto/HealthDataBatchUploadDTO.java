package com.health.assessment.dto;

import lombok.Data;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * 健康数据批量上传 DTO
 */
@Data
public class HealthDataBatchUploadDTO {

    @NotEmpty(message = "数据列表不能为空")
    @Valid
    private List<HealthDataUploadDTO> items;
}

