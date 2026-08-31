package com.board.article.paging.v1page;

import com.board.article.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class V1ArticleService {

    private final V1ArticleRepository v1ArticleRepository;

    //page 1부터 시작하는 페이지 번호 ageRequest는 0부터라 1을 빼서 넘긴다.
    public V1ArticlePageResponse readAll(Long boardId, int page, int pageSize, ArticleSortType sortType) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, sortType.getSort());

        // 이 한 줄에서 쿼리가 두 번 나간다. 컨텐츠 조회 + count(*).
        Page<Article> result = v1ArticleRepository.findByBoardId(boardId, pageable);

        return V1ArticlePageResponse.from(result);
    }
}
