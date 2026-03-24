package com.ubik.streakservice.infrastructure.adapter.in.web.mapper;

import com.ubik.streakservice.domain.model.PrivilegePolicyFactory;
import com.ubik.streakservice.domain.model.UserStreak;
import com.ubik.streakservice.infrastructure.adapter.in.web.dto.StreakResponse;
import org.springframework.stereotype.Component;

@Component
public class StreakDtoMapper {

    public StreakResponse toResponse(UserStreak streak) {
        return new StreakResponse(
                streak.userId(),
                streak.level().name(),
                streak.reservationsLast30Days(),
                streak.discountRate(),
                PrivilegePolicyFactory.getPolicy(streak.level()).getBenefits(),
                streak.calculatedAt()
        );
    }
}
