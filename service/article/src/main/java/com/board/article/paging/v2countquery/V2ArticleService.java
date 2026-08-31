package com.board.article.paging.v2countquery;

import com.board.article.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class V2ArticleService {

    /** {@link V2ArticleRepository#findByBoardIdWithCappedCount} 의 countQuery 에 박힌 상한과 같은 값. */
    private static final long COUNT_CAP = 10_000L;

    private final V2ArticleRepository v2ArticleRepository;

    /**
     * V1 : select count(*) from article where board_id = 1                              -- 전체 스캔
     * A: select count(*) from (select article_id from article
     *                              where board_id = 1 limit 10000) t                       -- 10,000 에서 중단
     *
     * 상한을 넘는 페이지는 조회 전에 거절한다. 이유는 아래 maxPage 계산 주석 참고.
     */
    public V2ArticlePageResponse readAllWithCappedCount(Long boardId, int page, int pageSize) {
        validate(page, pageSize);

        long maxPage = maxPage(pageSize);
        if (page > maxPage) {
            throw new PageLimitExceededException(page, maxPage);
        }

        // 정렬은 쿼리 안에 있으므로 Pageable 은 limit/offset 만 담는다.
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Article> result = v2ArticleRepository.findByBoardIdWithCappedCount(boardId, pageable);

        return V2ArticlePageResponse.of(result, "capped-count", COUNT_CAP);
    }

    /**
     * B. 카운트 쿼리를 Supplier로 미뤄서, 필요 없으면 아예 실행하지 않는다.
     *
     * PageableExecutionUtils.getPage가 판단해 주는 규칙
     *   content.size() < pageSize(마지막 페이지) -> 카운트 생략, total = offset + size
     *   그 외(중간 페이지) -> Supplier 실행, 즉 카운트 쿼리가 그대로 나간다.
     *
     * 이건 "카운트를 없애는" 기법이 아니라 불필요한 카운트를 안 하는 기법이다.
     * 게시글이 적은 게시판, 마지막 페이지, 검색 결과가 적은 경우에 효과가 크다.
     *
     * 람다 안의 countByBoardId 호출이 줄에서 실행되지 않는다는 점이 핵심이다.
     * getPage 내부에서 필요하다고 판단할 때만 호출된다.
     *
     * 이쪽은 상한이 없으므로 페이지 제한도 없다. 대신 깊은 페이지에서
     * offset 비용을 그대로 맞는다. (offset 5,999,970 기준 약 37초)
     */
    public V2ArticlePageResponse readAllWithLazyCount(Long boardId, int page, int pageSize) {
        validate(page, pageSize);

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        List<Article> content = v2ArticleRepository.findContent(boardId, pageable);

        Page<Article> result = PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> v2ArticleRepository.countByBoardId(boardId)
        );

        // 상한이 없는 전략이므로 countCap 은 0 을 넘겨 capped 판정을 끈다.
        return V2ArticlePageResponse.of(result, "lazy-count", 0L);
    }

    /**
     * 상한 안에서 제공할 수 있는 마지막 페이지 번호
     *
     * 카운트가 10,000에서 멈추므로 totalPages 도 10,000 / pageSize 를 넘을 수 없다.
     * 그 너머는 카운트가 뒷받침해 주지 못하는 구간이라 페이지 번호를 그릴 근거가 없다.
     *
     *   pageSize = 20  -> 500 페이지
     *   pageSize = 30  -> 333 페이지
     *
     * pageSize 가 상한보다 큰 극단적인 경우에도 1페이지는 열어 두려고 최소 1을 보장한다.
     */
    private long maxPage(int pageSize) {
        return Math.max(1L, COUNT_CAP / pageSize);
    }

    private void validate(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("page 는 1 이상이어야 합니다. 요청값: " + page);
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize 는 1 이상이어야 합니다. 요청값: " + pageSize);
        }
    }
}
