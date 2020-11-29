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
