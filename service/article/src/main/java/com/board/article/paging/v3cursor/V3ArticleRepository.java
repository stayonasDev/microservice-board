package com.board.article.paging.v3cursor;

import com.board.article.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 커서 유무에 따라 메서드가 둘로 갈린다.
 *
 * 첫 요청에는 커서가 없어서 where article_id < ? 조건을 붙일 수 없다.
 * JPQL 은 조건을 동적으로 빼는 문법이 없으므로 메서드를 나누는 게 가장 단순하다.
 * (동적 조건이 여러 개로 늘어나면 그때 QueryDSL 이나 Specification 을 꺼낸다.)
 *
 * 두 쿼리 모두 idx_board_id_article_id (board_id, article_id)를 그대로 탄다.
 * where의 두 컬럼과 order by 컬럼이 인덱스 구성과 일치하므로
 * 정렬도 인덱스 안에서 끝나고 filesort 가 생기지 않는다.
 *
 * limit은 JPQL 문법에 없어서 Pageable로 전달한다.
 * 서비스에서 PageRequest.of(0, size)를 넘기므로 offset 은 항상 0 이다.
 * 즉 Pageable을 페이징이 아니라 순수한 limit 지정용으로만 쓰는 것이다.
 */
@Repository
public interface V3ArticleRepository extends JpaRepository<Article, Long> {

    //첫 페이지. 커서가 없으므로 맨 앞에서부터 읽는다.
    @Query("select a from Article a " +
            "where a.boardId = :boardId " +
            "order by a.articleId desc")
    List<Article> findFirstPage(
            @Param("boardId") Long boardId,
            Pageable pageable
    );

    //두 번째 페이지부터. 커서보다 작은 id 만 읽으므로 앞쪽을 훑지 않는다.
    @Query("select a from Article a " +
            "where a.boardId = :boardId and a.articleId < :cursor " +
            "order by a.articleId desc")
    List<Article> findNextPage(
            @Param("boardId") Long boardId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
