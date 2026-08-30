package com.board.article.api;

import com.board.article.service.response.ArticlePageResponse;
import com.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
                "hi", "my content", 1L, 1L
        ));
        log.info("response:{}", response);

        assertThat(response.getArticleId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("hi");
    }

    @Test
    void readTest() {
        Long articleId = create(new ArticleCreateRequest("hi", "my content", 1L, 1L)).getArticleId();

        ArticleResponse response = read(articleId);
        log.info("response:{}", response);

        assertThat(response.getArticleId()).isEqualTo(articleId);
        assertThat(response.getContent()).isEqualTo("my content");
    }

    @Test
    void updateTest() {
        Long articleId = create(new ArticleCreateRequest("hi", "my content", 1L, 1L)).getArticleId();

        update(articleId, new ArticleUpdateRequest("hi 2", "my content 22"));

        ArticleResponse response = read(articleId);
        log.info("response:{}", response);

        assertThat(response.getTitle()).isEqualTo("hi 2");
        assertThat(response.getContent()).isEqualTo("my content 22");
    }

    @Test
    void deleteTest() {
        Long articleId = create(new ArticleCreateRequest("to delete", "my content", 1L, 1L)).getArticleId();

        delete(articleId);
        // 삭제됐으므로 조회는 실패해야 정상이다
        assertThatThrownBy(() -> read(articleId))
                .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        return restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    void delete(Long articleId) {
        // retrieve() 는 lazy 하므로 종단 연산까지 호출해야 요청이 실제로 전송된다
        restClient.delete()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&page=1&pageSize=30")
                .retrieve()
                .body(ArticlePageResponse.class);

        log.info("response.getArticleCount(): {}", response);
        for (ArticleResponse article : response.getArticles()) {
            log.info("articleId: {}", article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleResponse> articles1 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        log.info("firstPage");
        for(ArticleResponse articleResponse : articles1){
            log.info("articleResponse.getArticleId(): {}", articleResponse.getArticleId());
        }

        Long lastArticleId = articles1.get(articles1.size() - 1).getArticleId();
        List<ArticleResponse> articles2 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        log.info("secondPage");
        for(ArticleResponse articleResponse : articles2){
            log.info("articleResponse.getArticleId(): {}", articleResponse.getArticleId());
        }

    }

    @Getter
    @ToString
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}
