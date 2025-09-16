# Base image로 opnejdk 17을 사용
FROM openjdk:17-jdk-slim

# COPY랑 ADD 중 하나 골라서 사용하면 됨. ADD는 COPY + 추가 기능(압축 해제, URL(특정 url에서 다운 받아서 하고 싶을 때) 기능을 가짐.
COPY ./build/libs/be17-pickcook-0.0.1-SNAPSHOT.jar   /app.jar

# RUN = 컨테이너에서 실행할 명령어, but 컨테이너를 준비하기 위한 명령어들만 실행
# ex. RUN apt update
#     RUN apt install net-tools


# doccker run -dit ubuntu bash 처럼 특정 이미지에서 마지막에 명령어를 입력해서 실행할 때
# CMD = 입력한 명령어로 덮어쓰기 됨.
# ENTRYPOINT = 덮어쓰기 되지 않음.
CMD ["java", "-jar", "/app.jar"]


# (안 써도 상관없음. 컨테이너 실행할 때 옵션으로 지정하면 됨. 문서화 역할, 기본값 설정(expose는 기본값 설정 안 됨))
#ENV = 환경 변수 설정
#EXPOSE = 포트포워딩 설정
EXPOSE 8080