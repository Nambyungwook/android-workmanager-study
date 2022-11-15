WorkManager Study
===================================

https://developer.android.com/codelabs/android-workmanager?hl=ko#0 를 따라서 공부한 내용 정리

WorkManager 기본사항
--------------

* Worker : 백그라운드에서 실행하고자 하는 작업의 코드를 입력, 이 클래스를 확장하고 doWork() 메소드 재정의
* WorkRequest : 작업 실행 요청을 나타냄, WorkRequest를 만드는 과정에서 Worker를 전달, WorkRequest를 만들때 Worker를 실행할 시점에 적용되는 Constrants 등을 지정 할 수 있음
* WorkManager : 이 클래스는 WorkRequest를 예약하고 실행, 지정된 제약 조건을 준수하면서 시스템 리소스에 부하를 분산하는 방식으로 WorkRequest 예약

Data 객체
--------------

* 입력 및 출력을 전달
* Data 객체는 키-값 쌍의 경량 컨데이너
* WorkRequest의 내/외부로 전달될 수 있는 소량의 데이터를 저장하기 위해 사용

WorkManager 작업 체인
--------------

* WorkManager를 사용하면 순서대로 실행되거나 동시에 실행되는 별도의 WorkRequest를 만들 수 있음
* WorkRequest의 출력이 체인 내 다음 WorkRequest의 입력이 됨

고유 작업 체인 (Unique work chains)
--------------
* 작업 체인을 한 번에 하나씩만 실행해야 하는 경우에 첫 번째 데이터 동기화가 완료된 후에 새 동기화가 시작되도록 할 수 있음
* beginWith 대신 beginUniqueWork를 사용하고 고유한 String 이름을 제공
* 함께 참조하고 쿼리할 수 있도록 전체 작업 요청 체인을 지정

WorkInfo 객체
--------------
* WorkInfo : WorkRequest의 현재 상태에 관현 세부정보를 포함한 객체
* 작업의 상태 - BLOCKED, CANCELLED, ENQUEUED, FAILED, RUNNING, SUCCEEDED
* WorkRequest가 완료된 경우 작업의 모든 출력 데이터
* WorkInfo에는 저장된 최종 Data 객체를 가져올 수 있는 getOutputData메소드가 있으며 Kotlin에서는 outputData를 사용하여 메소드에 접근할 수 있음
* WorkInfo 객체가 포함된 LiveData를 가져와서 WorkRequest의 상태를 가져올 수 있음
* LiveData<WorkInfo> 객체나 LiveData<List<WorkInfo>> 객체를 가져오는 방법 및 결과
* ID를 사용하여 작업 가져오기 - getWorkInfoByIdLiveData - 각 WorkRequest에는 WorkManager에서 생성된 고유 ID가 있으며 ID를 사용하여 바로 WorkRequest의 단일 LiveData를 얻을 수 있음
* 고유 체인 이름을 사용하여 작업 가져오기 - getWorkInfosForUniqueWorkLiveData - WorkRequest는 고유 체인에 포함될 수 있으며 이 메서드는 고유한 단일 WorkRequests 체인에 있는 모든 작업의 LiveData를 반환
* 태그를 사용하여 작업 가져오기 - getWorkInfosByTagLiveData - 선택적으로 WorkRequest를 String으로 태그 지정할 수 있으며 동일한 태그를 사용하여 여러 WorkRequest를 태그하여 연결할 수 있음, 이 메서드는 단일 태그의 LiveData를 반환

WorkManager Constrants(제약 조건)
--------------
* Constrants 객체를 생성하고 setConstraints()를 사용하여 WorkRequest에 설정할 수 있음
* 제약 조건의 종류는 네트워크종류, 배터리, 저장공간 등등으로 https://developer.android.com/reference/androidx/work/Constraints.Builder.html?hl=ko서 확인할 수 있음