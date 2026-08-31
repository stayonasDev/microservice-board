package com.board.article.paging.v3cursor;

import com.board.article.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class V3ArticleService {

    private final V3ArticleRepository v3ArticleRepository;

    /**
     * cursor 직전 응답의 nextCursor, 첫 요청이면 null
     */
    public V3ArticleCursorResponse readAll(Long boardId, int pageSize, Long cursor) {
        // pageSize + 1 개를 요청한다. 초과분 1개가 "다음이 있다"는 신호다.
        Pageable limit = PageRequest.of(0, pageSize + 1);

        List<Article> fetched = (cursor == null)
                ? v3ArticleRepository.findFirstPage(boardId, limit)
                : v3ArticleRepository.findNextPage(boardId, cursor, limit);

        boolean hasNext = fetched.size() > pageSize;

        // 탐침으로 더 읽은 1개는 응답에서 잘라낸다.
        List<Article> articles = hasNext
                ? fetched.subList(0, pageSize)
                : fetched;

        return V3ArticleCursorResponse.of(articles, hasNext);
    }
}
