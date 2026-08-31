package com.board.article.paging.v2countquery;

import com.board.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface V2ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * A — 카운트 쿼리를 직접 지정한다.
     *
     * 핵심은 countQuery
     * 자동 생성이었다면 select count(*) from article where board_id = ?가 나가서
     * 조건에 맞는 행을 전부 훑었을 것이다. 여기서는 파생 테이블 + limit으로
     * 10,000개까지만 세고 멈춘다. 안쪽 서브쿼리가 article_id만 읽으므로
     * 커버링 인덱스에서 끝나고 클러스터드 인덱스를 건드리지 않는다.
     *
     * 대가는 정확도다. 게시글이 10,000 개를 넘으면 totalCount는 항상 10,000 으로 나온다.
     * "10,000+ 건" 으로 표시하거나 페이지 버튼을 그 범위까지만 그리는 UI 를 전제로 한다.
     * 상한값이 리터럴인 이유는 애노테이션 값이 컴파일 타임 상수여야 하기 때문이다.
     * 페이지 번호에 따라 상한을 움직이고 싶다면 Page를 포기하고
     * PageLimitCalculator로 가야 한다.
     *
     * Pageable에는 정렬을 담지 않는다. 네이티브 쿼리에 Sort가 붙으면
     * Spring이 SQL 문자열을 재작성하는데 그게 깨지기 쉬워 정렬은 쿼리 안에 직접 쓰고
     * Pageable은 limit/offset 용도로만 쓴다.
     */
    @Query(
            value = "select * from article " +
                    "where board_id = :boardId " +
                    "order by article_id desc",
            countQuery = "select count(*) " +
                    "from (" +
                    "  select article_id from article " +
                    "  where board_id = :boardId " +
                    "  limit 10000" +
                    ") t",
            nativeQuery = true
    )
    Page<Article> findByBoardIdWithCappedCount(@Param("boardId") Long boardId, Pageable pageable);


     // B - 반환 타입이 List라서 카운트 쿼리가 나가지 않는다.
     // 카운트를 붙일지 말지는 서비스에서 결정한다.
    @Query("select a from Article a where a.boardId = :boardId order by a.articleId desc")
    List<Article> findContent(@Param("boardId") Long boardId, Pageable pageable);


     //B - Supplier 안에 감싸져 필요할 때만 호출된다.
    @Query("select count(a) from Article a where a.boardId = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);
}
