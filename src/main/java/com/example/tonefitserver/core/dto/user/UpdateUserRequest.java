package com.example.tonefitserver.core.dto.user;

import com.example.tonefitserver.core.enums.CareerLevel;
import com.example.tonefitserver.core.enums.Industry;

/**
 * PATCH /users/me 요청. 보낼 필드만 채워서 보낸다.
 */
public record UpdateUserRequest(
        String nickname,
        Industry industry,
        CareerLevel careerLevel
) {
}
