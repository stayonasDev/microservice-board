package com.board.article.paging.v2countquery;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 두 기법을 각각 다른 엔드포인트로 열어서 실행 SQL 을 비교할 수 있게 했다.
 * show-sql: true가 켜져 있으니, lazy-count를 마지막 페이지로 호출하면
 * 카운트 쿼리가 로그에 찍히지 않는 것을 직접 확인할 수 있다.
 *
 * capped-count 는 상한(10,000건) 너머의 페이지를 400 으로 거절한다.
 * 예외 처리를 전역 @RestControllerAdvice 가 아니라 이 컨트롤러 안에 둔 것은
 * 폴더 하나만 열면 그 방식이 처음부터 끝까지 보이게 하려는 의도이고,
 * 다른 페이징 방식의 응답에 영향을 주지 않기 위해서다.
 */
@RestController
@RequiredArgsConstructor
public class V2ArticleController {

    private final V2ArticleService v2ArticleService;

    @GetMapping("/paging/v2/articles/capped-count")
    public V2ArticlePageResponse readAllWithCappedCount(
            @RequestParam Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize
    ) {
        return v2ArticleService.readAllWithCappedCount(boardId, page, pageSize);
    }

    @GetMapping("/paging/v2/articles/lazy-count")
    public V2ArticlePageResponse readAllWithLazyCount(
            @RequestParam Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize
    ) {
        return v2ArticleService.readAllWithLazyCount(boardId, page, pageSize);
    }

     //maxPage 를 응답에 같이 실어 준다.
     //프론트가 "몇 페이지까지 그려도 되는지" 를 이 값으로 알 수 있어야
     //사용자가 벽에 부딪히기 전에 버튼을 막을 수 있다.
    @ExceptionHandler(PageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handlePageLimitExceeded(PageLimitExceededException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", e.getMessage(),
                "requestedPage", e.getRequestedPage(),
                "maxPage", e.getMaxPage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", e.getMessage()
        ));
    }
}
