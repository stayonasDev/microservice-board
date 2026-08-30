## 설치 가이드
```bash
$ docker pull mysql:8.0.38
$ docker run --name [컨테이너 이름] -e MYSQL_ROOT_PASSWORD=[환경 변수 루트 비밀번호] -d -p 3306:3306 mysql:8.0.36

# PS 확인 및 images 확인
$ docker ps 
$ docker ps -a
$ docker images

# 컨테이너 접속 방법 
# 1.bash로 컨테이너 접속 후 MySQL 접속
$ docker exec -it [컨테이너 이름] bash
bash-5.1# mysql -uroot -p[설정한 비밀번호]

# 2.바로 Mysql 접속
$ docker exec -it [컨테이너 이름] myslq -uroot -p[설정한 비밀번호
```


## Article SQL
### 페이징
```bash
# 현재 더미 데이터 삽입으로 약 1천 200만건 게시물이 있다. (테스트 로직 실행)
# article의 resources/db/ddl.sql 적용 편리하게 적용하려면 docker compose로 실행 시 자동 삽입 및 JPA ddl-auto: create 가능
$ docker exec -it [컨테이너 이름] mysql -uroot -proot
mysql> select * from article where board_id = 1 order by created_at desc limit 30 offset 90;
# 30 rows in set (6.39 sec)

# 실행 계획 확인(실제 옵티마이저 실행 계획을 보려면 analyze 추가)
mysql> explain select * from article where board_id = 1 order by created_at desc limit 30 offset 90;
# type ALL -> pull table scan
# Extra Using where; Using filesort - > where 조건에 대해 필터링, 데이터가 많아 정렬을 수행할 수 없어 파일(디스크)에서 데이터를 정렬하는 filesort 수행

# 인덱스 생성
# 생성 시간으로 정렬, 분산 서버는 created_at 중복 발생 가능성 높음 snowflake로 중복을 최소화하고 오름차순 정렬 가능하게 한다.
mysql> create index_idx_board_id_article_id on aritcle(board_id, asc, article_id desc);

# 인덱스 적용 후 쿼리 실행 계획 보기
mysql> explain select * from article where board_id = 1 order by created_at desc limit 30 offset 90;
# key | idx_board_id_article_id -> 인덱스 사용 중

# offset을 높여서 실행 (30 rows in set (6.12 sec)
mysql> select * from article where board_id = 1 order by created_at desc limit 30 offset 1499970;

# offset이 높아질 수록(페이지 번호가 늘어날 수록) 느려지는 이유는 offset 100번 limit 30번이라면
# 읽을 필요 없는 100번 전에 데이터를 읽고 버리고, Non Clustered Index 조회 후 Clustered Index를 읽기 때문
# 커버링 인덱스로 id를 추출하고 Clustered Index에 접근하는 방법
mysql> select board_id, article_id from article where board_id = 1 order by article_id desc limit 30 offset 1499970;
mysql> select * from(
       select article_id from article
       where board_id = 1
       order by article_id desc
       limit 30 offset 1499970
) t left join article on t.article_id = article.article_id;
# 쿼리 플랜을 실행하면 DERIVED 파생 테이블 사용, Using Index 커버링 인덱스 사용, PRIMARY 클러스터 인덱스에서 데이터를 가져오는 것을 확인 가능
# 하지만 offset을 늘리면 0.21 sec -> 0.98 sec로 늘어남
# 해결 방법은 데이터를 1년 단위로 분리하여 관리하거나 무한 스크롤이 있다.
# JPA에서 페이징 쿼리에 대한 자료는 많기 때문에 Cursor 적용과 Batch Size 적용을 적용하면 더욱 효과적이다.

# 페이지 번호 공식
# 현재 페이지 n(n > 0), 페이지 당 게시글 개수 m, 이동 가능한 페이지 개수 k, ((n - 1) / k)의 나머지 버림
# (((n - 1) / k) + 1) * m * k + 1
# n = 7, m = 30, k = 10
# (((7 - 1) / 10) + 1) * 30 * 10 + 1 = 301
# 커버링 인덱스 사용, count query는 limit 불가
mysql> select count(*) from (select article_id from article where board_id = 1 limit 300301) t;
```

### 무한 스크롤
```bash
# 무한 스크롤은 번호 방식의 페이징을 사용하면 중복/누락 문제 발생
# 조회한 마지막 행의 id를 사용
mysql> select * from article where board_id = 1 order by article_id desc limit 30;

# 마지막 행의 id를 사용해서 쿼리 -> offset과 달리 아무리 뒷 페이지로 가도 균등한 속도 보장, 인덱스 사용
mysql> select * from article where board_id = 1 and article_id < 352018473706639590 order by article_id desc limit 30;
```