package com.board.article.paging.v1page;

import com.board.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Page<Article>을 반환 타입으로 두면 Spring Data 가 컨텐츠 쿼리와 카운트 쿼리를
 * 둘 다 만들어서 실행하고, 그 결과를 Page에 담아준다. @Query도 필요 없다.
 *
 * 참고로 반환 타입만 바꾸면 동작이 달라진다.
 * Page<Article> — 카운트 쿼리 실행, 전체 건수/전체 페이지 수를 알 수 있다
 * Slice<Article> — 카운트 안 함. pageSize+1 을 조회해서 hasNext 만 판단
 * List<Article> — 카운트도 hasNext 도 없음. 그냥 limit/offset 만 적용
 */
@Repository
public interface V1ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByBoardId(Long boardId, Pageable pageable);
}
