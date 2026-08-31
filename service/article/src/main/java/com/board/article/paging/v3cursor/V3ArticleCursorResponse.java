package com.board.article.paging.v3cursor;

import com.board.article.entity.Article;
import com.board.article.service.response.ArticleResponse;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 커서 응답에는 totalCount도 totalPages도 없다.
 * 알 수 없어서가 아니라 알려면 O(N) 카운트를 해야 하는데 커서 UI 는 그게 필요 없기 때문이다.
 *
 * 클라이언트는 nextCursor를 그대로 다음 요청에 실어 보내면 된다.
 * hasNext가 false면 nextCursor는 null이다.
 */
@Getter
@ToString
public class V3ArticleCursorResponse {

    private List<ArticleResponse> articles;

    //다음 요청에 그대로 넘길 커서, 더 없으면 null
    private Long nextCursor;

    private boolean hasNext;

    public static V3ArticleCursorResponse of(List<Article> articles, boolean hasNext) {
        V3ArticleCursorResponse response = new V3ArticleCursorResponse();
        response.articles = articles.stream()
                .map(ArticleResponse::from)
                .toList();
        response.hasNext = hasNext;
        response.nextCursor = hasNext
                ? articles.get(articles.size() - 1).getArticleId()
                : null;
        return response;
    }
}
