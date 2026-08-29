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