import org.kohsuke.github.*;

import java.io.IOException;

public class CheckIssueReplyUser {
    public static void main(String[] args) throws IOException {
        //사용자 이름 및 암호를 통해 연결
        System.out.println("깃허브 연결 시작");
        String my_personal_token = "b095b738240e2f5ce86ed66596dc52d28c622c65";
        GitHub github = new GitHubBuilder().withOAuthToken(my_personal_token).build();
        System.out.println("깃허브 연결 완료");


        System.out.println("organization get");
        GHOrganization newGOrganization = organizationClient(github, "KilGithubOrganization");
        System.out.println("organization get suc");
        System.out.println("repo get");
        GHRepository ghRepositor = newGOrganization.getRepository("git-hub-api-test-repo");
        System.out.println("repo get suc");


        GHIssue ghIssue = ghRepositor.getIssue(1);
        PagedIterable<GHIssueComment> ghIssueComments = ghIssue.listComments();
        for (GHIssueComment ghIssueComment : ghIssueComments) {
            System.out.println(ghIssueComment.getUser().getName());
        }


    }

    static GHOrganization organizationClient(GitHub gitHub, String organizationName) throws IOException {
        return gitHub.getOrganization(organizationName);
    }
}
