package com.codekoi.domain.user.usecase;

import com.codekoi.domain.user.entity.User;

public interface QueryUserByEmailUseCase {

    User query(Query query);

    record Query(
            String email
    ) {
    }

}