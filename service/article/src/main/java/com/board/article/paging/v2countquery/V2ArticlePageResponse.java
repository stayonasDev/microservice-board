package com.board.article.paging.v2countquery;

import com.board.article.entity.Article;
import com.board.article.service.response.ArticleResponse;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@ToString
public class V2ArticlePageResponse {

    private List<ArticleResponse> articles;
    private long page;
    private long pageSize;
    private long totalCount;
    private long totalPages;
    private boolean hasNext;

     //어떤 카운트 전략이 쓰였는지 눈으로 보려고 넣은 학습용 필드.
     //실제 API 라면 이런 건 응답에 넣지 않는다.
    private String countStrategy;

     // totalCount가 상한에 걸려 잘린 값인지 알려주는 플래그.
     // 프론트가 "10,000+" 처럼 표시할지 결정할 때 쓴다.
    private boolean countCapped;

    public static V2ArticlePageResponse of(Page<Article> page, String countStrategy, long countCap) {
        V2ArticlePageResponse response = new V2ArticlePageResponse();
        response.articles = page.getContent().stream()
                .map(ArticleResponse::from)
                .toList();
        response.page = page.getNumber() + 1L;
        response.pageSize = page.getSize();
        response.totalCount = page.getTotalElements();
        response.totalPages = page.getTotalPages();
        response.hasNext = page.hasNext();
        response.countStrategy = countStrategy;
        response.countCapped = countCap > 0 && page.getTotalElements() >= countCap;
        return response;
    }
}
