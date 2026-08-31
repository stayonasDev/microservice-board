package com.board.article.paging.v2countquery;

/**
 * 상한(COUNT_CAP)을 넘는 페이지를 요청했을 때 던진다.
 *
 * 상한 방식의 구멍을 막기 위한 예외다. countQuery의 limit은 "세는 것"만 멈출 뿐
 * "읽는 것"은 막지 못한다. 그래서 상한 너머의 페이지를 요청하면
 * 글은 나오는데 totalPages가 요청 페이지를 따라 늘어나고 hasNext는 계속 false인
 * 어중간한 상태가 된다. 데이터는 보이는데 다음으로는 못 가는 상태다.
 *
 * 상한을 뒀다는 건 "여기까지만 서비스한다"는 정책을 세운 것이므로, 그 정책을 응답으로도 지킨다.
 * 구글이 검색 결과를 일정 페이지에서 끊는 것과 같은 처리다.
 */
public class PageLimitExceededException extends RuntimeException {

    private final long requestedPage;
    private final long maxPage;

    public PageLimitExceededException(long requestedPage, long maxPage) {
        super("%d페이지는 조회할 수 없습니다. 이 API는 %d페이지까지만 제공합니다.".formatted(requestedPage, maxPage));
        this.requestedPage = requestedPage;
        this.maxPage = maxPage;
    }

    public long getRequestedPage() {
        return requestedPage;
    }

    public long getMaxPage() {
        return maxPage;
    }
}
