package com.board.article.paging.v1page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

/**
 * 정렬 옵션 화이트리스트.
 *
 * Pageable의 Sort를 프론트에서 그대로 받으면 두 가지 사고가 난다.
 *   성능 — 인덱스에 없는 컬럼으로 정렬하면 filesort가 걸려 대용량에서 타임아웃
 *   스키마 노출 — ?sort=writer_id,desc 같은 요청이 그대로 SQL 컬럼명이 된다.
 *
 * 그래서 정렬 가능한 조합만 enum 으로 열어두고 각각에 대응하는 인덱스를 미리 만들어 둔다.
 * 여기 두 옵션은 모두 idx_board_id_article_id (board_id, article_id)} 를 그대로 탄다.
 *
 * 제목순 order by title 같은 옵션을 넣지 않은 이유가 바로 그것이다.
 * title에는 인덱스가 없어서 게시판이 커지는 순간 죽는다.
 * 정말 필요하면 옵션을 추가하기 전에 인덱스부터 만들어야 한다.
 *
 * 정렬 기준이 createdAt이 아니라 articleId인 것도 의도된 선택이다.
 * PK가 스노우플레이크라 시간 순으로 증가하므로 생성시각 정렬을 PK 정렬로 대체할 수 있고
 * 그 덕분에 정렬이 인덱스 안에서 끝난다.
 */
@Getter
@RequiredArgsConstructor
public enum ArticleSortType {

    // 최신순, idx_board_id_article_id 를 역방향으로 스캔한다.
    LATEST(Sort.by(Sort.Direction.DESC, "articleId")),

    // 등록순, 같은 인덱스를 정방향으로 스캔한다.
    OLDEST(Sort.by(Sort.Direction.ASC, "articleId"));

    private final Sort sort;
}
