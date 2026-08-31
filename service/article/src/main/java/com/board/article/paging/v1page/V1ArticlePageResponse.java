package com.board.article.paging.v1page;

import com.board.article.entity.Article;
import com.board.article.service.response.ArticleResponse;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Page를 그대로 내리지 않고 감싸는 이유.
 *
 * Page를 컨트롤러에서 그대로 반환하면 pageable, sort, empty
 * 같은 Spring 내부 구조가 JSON 에 그대로 노출된다. 그 구조는 라이브러리 버전에 따라 바뀌기 때문에
 * 스프링을 올리는 순간 API 스펙이 깨진다.
 * (Spring Data Commons 3.3 부터는 PageImpl을 직접 직렬화하면 경고를 남기고
 * PagedModel사용을 권한다. 이 프로젝트는 Boot 3.5.9 라 해당된다.)
 *
 * 그래서 필요한 필드만 골라 담은 자체 DTO로 감싼다.
 */
@Getter
@ToString
public class V1ArticlePageResponse {

    private List<ArticleResponse> articles;

    // 현재 페이지, API 는 1을 받고 Pageable 은 0부터 시작한다. 여기서 다시 되돌린다.
    private long page;
    private long pageSize;

    // 전체 건수, 장점이자 비용의 근원.
    private long totalCount;
    private long totalPages;

    private boolean hasNext;
    private boolean hasPrevious;

    public static V1ArticlePageResponse from(Page<Article> page) {
        V1ArticlePageResponse response = new V1ArticlePageResponse();
        response.articles = page.getContent().stream()
                .map(ArticleResponse::from)
                .toList();
        response.page = page.getNumber() + 1L;
        response.pageSize = page.getSize();
        response.totalCount = page.getTotalElements();
        response.totalPages = page.getTotalPages();
        response.hasNext = page.hasNext();
        response.hasPrevious = page.hasPrevious();
        return response;
    }
}
