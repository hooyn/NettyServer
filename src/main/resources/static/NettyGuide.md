# Netty

## Getting Start Netty

- 서블릿 기반 스프링 (스프링 4.0까지)
- 사용자의 Request가 올 때마다 쓰레드가 만들어집니다.
- 쓰레드가 많아지면 컨텍스트 스위칭이 많아져서 성능이 안좋다.

* 스프링 5.0에서는 비동기로 한다.
* Request가 오면 대기열에 넣어서 처리
* 서버는 대기열을 확인해서 완료된 일에 대해서 처리한다.
* Netty가 비동기 서버이므로 성능이 좋다.
* Mongo DB도 비동기로 처리가 가능하다.

- 