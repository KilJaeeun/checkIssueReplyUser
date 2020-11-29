# 백기선 4주차 과제 1
* 과제 링크 : https://github.com/whiteship/live-study/issues/4

* 과제 설명

과제 1. live-study 대시 보드를 만드는 코드를 작성하세요.
깃헙 이슈 1번부터 18번까지 댓글을 순회하며 댓글을 남긴 사용자를 체크 할 것.
참여율을 계산하세요. 총 18회에 중에 몇 %를 참여했는지 소숫점 두자리가지 보여줄 것.
Github 자바 라이브러리를 사용하면 편리합니다.
깃헙 API를 익명으로 호출하는데 제한이 있기 때문에 본인의 깃헙 프로젝트에 이슈를 만들고 테스트를 하시면 더 자주 테스트할 수 있습니다.

* 사용 라이브러리
https://github.com/hub4j/github-api

```
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.antlr.runtime.misc.Stats.sum;

public class CheckIssueReplyUser {
    public static void main(String[] args) throws IOException {
        //사용자 이름 및 암호를 통해 연결
        String my_personal_token = "abcde";
        GitHub github = new GitHubBuilder().withOAuthToken(my_personal_token).build();
        // 깃헙 레포 연결
        GHRepository ghRepositor = github.getRepository("helloTestRepo");

        // 참여자 저장소 생성
        Map<String, int[]> participantHashMap = new HashMap<>();
        // 참여여부 체크
        for (int issueNum = 1; issueNum < 19; issueNum++) {
            GHIssue ghIssue = ghRepositor.getIssue(issueNum);
            PagedIterable<GHIssueComment> ghIssueComments = ghIssue.listComments();
            for (GHIssueComment ghIssueComment : ghIssueComments) {
                String participant = ghIssueComment.getUser().getName();
                if (participantHashMap.containsKey(participant)) {

                    int[] assignments = participantHashMap.get(participant);
                    assignments[issueNum] = 1;
                } else {

                    int[] assignments = new int[19];// 초기값 0
                    assignments[issueNum] = 1;
                    participantHashMap.put(participant, assignments);


                }
            }

        }
        // 참여자 전체 정보 출력
        participantHashMap.forEach((key, value) -> {
            float countPercent = (sum(value) * 100) / 18;
            System.out.print("참여자명: " + key);
            System.out.println(", 참여율: " + String.format("%.2f", countPercent) + "%");
        });

    }


}

```