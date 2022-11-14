WorkManager Study
===================================

https://developer.android.com/codelabs/android-workmanager?hl=ko#0 를 따라서 공부한 내용 정리

WorkManager 기본사항
--------------

* Worker : 백그라운드에서 실행하고자 하는 작업의 코드를 입력, 이 클래스를 확장하고 doWork() 메소드 재정의
* WorkRequest : 작업 실행 요청을 나타냄, WorkRequest를 만드는 과정에서 Worker를 전달, WorkRequest를 만들때 Worker를 실행할 시점에 적용되는 Constrants 등을 지정 할 수 있음
* WorkManager : 이 클래스는 WorkRequest를 예약하고 실행, 지정된 제약 조건을 준수하면서 시스템 리소스에 부하를 분산하는 방식으로 WorkRequest 예약