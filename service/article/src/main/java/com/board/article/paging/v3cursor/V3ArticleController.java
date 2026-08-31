package com.board.article.paging.v3cursor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * page 파라미터가 없다는 점이 방식 1, 2 와의 가장 큰 차이다.
 * 클라이언트는 "몇 번째 페이지"를 지정할 수 없고, 서버가 준 커서만 되돌려줄 수 있다.
 * 이 제약이 곧 성능 보장의 근거
 */
@RestController
@RequiredArgsConstructor
public class V3ArticleController {

    private final V3ArticleService v3ArticleService;

    @GetMapping("/paging/v4/articles")
    public V3ArticleCursorResponse readAll(
            @RequestParam Long boardId,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(required = false) Long cursor
    ) {
        return v3ArticleService.readAll(boardId, pageSize, cursor);
    }
}
