package com.codekoi.apiserver.skill.review.service;

import com.codekoi.apiserver.comment.repository.ReviewCommentQueryRepository;
import com.codekoi.apiserver.review.repository.CodeReviewQueryRepository;
import com.codekoi.apiserver.skill.review.dto.UserSkillStatistics;
import com.codekoi.apiserver.skill.review.repository.CodeReviewSkillQueryRepository;
import com.codekoi.apiserver.utils.ServiceTest;
import com.codekoi.domain.comment.ReviewComment;
import com.codekoi.domain.review.CodeReview;
import com.codekoi.domain.skill.review.CodeReviewSkill;
import com.codekoi.domain.skill.skill.Skill;
import com.codekoi.domain.user.User;
import com.codekoi.domain.user.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static com.codekoi.fixture.CodeReviewFixture.REVIEW1;
import static com.codekoi.fixture.CodeReviewFixture.REVIEW2;
import static com.codekoi.fixture.ReviewCommentFixture.REVIEW_COMMENT;
import static com.codekoi.fixture.SkillFixture.JPA;
import static com.codekoi.fixture.SkillFixture.SPRING;
import static com.codekoi.fixture.UserFixture.HONG;
import static com.codekoi.fixture.UserFixture.SUNDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;


class CodeReviewSkillQueryServiceTest extends ServiceTest {

    @Mock
    private CodeReviewQueryRepository codeReviewQueryRepository;
    @Mock
    private ReviewCommentQueryRepository reviewCommentQueryRepository;
    @Mock
    private CodeReviewSkillQueryRepository codeReviewSkillQueryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CodeReviewSkillQueryService codeReviewSkillQueryService;

    @Nested
    class 유저의_스킬_태그_통계_조회_테스트 {

        @Test
        void 코드리뷰를_요청하고_다른_리뷰건에_댓글을_남기면_통계에_둘다_반영된다() {
            //given
            final User user = SUNDO.toUser(1L);
            given(userRepository.getOneById(any()))
                    .willReturn(user);

            final CodeReview codeReview = REVIEW1.toCodeReview(2L, user);
            given(codeReviewQueryRepository.findByUserId(anyLong()))
                    .willReturn(List.of(codeReview));

            final Skill skill1 = SPRING.toSkill(1L);
            codeReview.addCodeReviewSkill(skill1);

            final User other = HONG.toUser();
            final CodeReview otherReview = REVIEW2.toCodeReview(3L, other);
            final Skill skill2 = JPA.toSkill(2L);
            otherReview.addCodeReviewSkill(skill2);
            final ReviewComment reviewComment = REVIEW_COMMENT.toCodeReviewComment(4L, user, otherReview);
            given(reviewCommentQueryRepository.findByUserId(anyLong()))
                    .willReturn(List.of(reviewComment));

            final CodeReviewSkill reviewSkill1 = codeReview.getSkills().get(0);
            final CodeReviewSkill reviewSkill2 = otherReview.getSkills().get(0);
            given(codeReviewSkillQueryRepository.findByCodeReviewIdIn(any()))
                    .willReturn(List.of(reviewSkill1, reviewSkill2));

            //when
            final List<UserSkillStatistics> statistics = codeReviewSkillQueryService.findUserSkillStatistics(user.getId());

            assertThat(statistics).hasSize(2);
            assertThat(statistics)
                    .extracting("id", "name", "count")
                    .containsExactlyInAnyOrder(
                            tuple(skill2.getId(), skill2.getName(), 1),
                            tuple(skill1.getId(), skill1.getName(), 1)
                    );
        }

        @Test
        void 하나의_코드리뷰에_여러개의_댓글을_남기면_하나의_댓글만_통계에_반영한다() {
            //given
            final User user = SUNDO.toUser(1L);
            given(userRepository.getOneById(any()))
                    .willReturn(user);

            final CodeReview codeReview = REVIEW1.toCodeReview(2L, user);
            given(codeReviewQueryRepository.findByUserId(anyLong()))
                    .willReturn(List.of(codeReview));

            final Skill skill1 = SPRING.toSkill(1L);
            codeReview.addCodeReviewSkill(skill1);

            final User other = HONG.toUser();
            final CodeReview otherReview = REVIEW2.toCodeReview(3L, other);
            final Skill skill2 = JPA.toSkill(2L);
            otherReview.addCodeReviewSkill(skill2);
            final ReviewComment reviewComment = REVIEW_COMMENT.toCodeReviewComment(4L, user, codeReview);
            given(reviewCommentQueryRepository.findByUserId(anyLong()))
                    .willReturn(List.of(reviewComment));

            final CodeReviewSkill reviewSkill1 = codeReview.getSkills().get(0);
            final CodeReviewSkill reviewSkill2 = otherReview.getSkills().get(0);
            given(codeReviewSkillQueryRepository.findByCodeReviewIdIn(any()))
                    .willReturn(List.of(reviewSkill1, reviewSkill2));

            final List<UserSkillStatistics> statistics = codeReviewSkillQueryService.findUserSkillStatistics(user.getId());


            assertThat(statistics).hasSize(1);
        }
    }
}