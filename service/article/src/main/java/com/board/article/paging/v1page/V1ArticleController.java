package com.board.article.paging.v1page;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * sort는 ArticleSortType 이름만 받는다. 임의의 컬럼명을 받지 않으므로
 * 값이 잘못되면 SQL 까지 가지 않고 400 으로 끝난다.</p>
 */
@RestController
@RequiredArgsConstructor
public class V1ArticleController {

    private final V1ArticleService v1ArticleService;

    @GetMapping("/paging/v1/articles")
    public V1ArticlePageResponse readAll(
            @RequestParam Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(defaultValue = "LATEST") ArticleSortType sort
    ) {
        return v1ArticleService.readAll(boardId, page, pageSize, sort);
    }
}
