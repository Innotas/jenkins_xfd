package com.teratory.xfd.github;

//import org.apache.commons.io.IOUtils;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClients;

import com.teratory.xfd.github.model.query.PrsAssignedToMeQuery;
import com.teratory.xfd.github.model.query.PullRequest;
import com.teratory.xfd.github.model.query.WhoAmIQuery;
import com.teratory.xfd.github.model.query.assignedprs.Edge;
import io.aexp.nodes.graphql.*;
import io.aexp.nodes.graphql.exceptions.GraphQLException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GitHubConnector {

    private final String userName;
    private String apiKey;
    protected String apiHostName = "api.github.com";

    public GitHubConnector(String apiKey) {
        this.apiKey = apiKey;
        try {
            this.userName = loadUserName();
        } catch (GraphQLException e) {
            e.printStackTrace();
            throw new GraphQLException("Could not connect to GitHub: " + e);
        }
    }

    public int getPullRequestsAssignedToMeCount() throws IOException {
        return getPullRequestsAssignedToMe().size();
    }

    public boolean hasPullRequestsAssignedToMe() throws IOException {
        return !getPullRequestsAssignedToMe().isEmpty();
    }

    protected GraphQLRequestEntity.RequestBuilder createRequestBuilder() throws GraphQLException {
        String gitHubApiUrl = "https://" + apiHostName + "/graphql";
        try {
            return GraphQLRequestEntity.Builder()
                    .url(gitHubApiUrl)
                    .headers(Collections.singletonMap("Authorization", "bearer " + apiKey));
        } catch (MalformedURLException e) {
            throw new GraphQLException("Invalid URL: " + gitHubApiUrl + " (" + e.getMessage() + ")");
        }
    }

    protected String loadUserName() throws GraphQLException {
        GraphQLResponseEntity<WhoAmIQuery> response = new GraphQLTemplate().query(createRequestBuilder().request(WhoAmIQuery.class).build(), WhoAmIQuery.class);
        return response.getResponse().viewer.login;
    }

    protected List<PullRequest> getPullRequestsAssignedToMe() throws IOException {
        try {
            GraphQLRequestEntity request = createRequestBuilder()
                    .request(
                        "query { " +
                            "search(query:\"assignee:" + userName + " is:pr is:open\", type:ISSUE,last:100) { " +
                                "edges { " +
                                    "node { " +
                                        "... on PullRequest { " +
                                            "title " +
                                            "url " +
                                            "number " +
                                            "assignees(last:100) { " +
                                                "edges { " +
                                                    "node { " +
                                                        "login " +
                                                    "} " +
                                                "} " +
                                            "} " +
                                        "} " +
                                    "} " +
                                "} " +
                            "} " +
                        "}"
                    )
                    // It would be great to use the annotations provided by americanexpress/nodes, but their syntax does not
                    // see to have a way to represent the "... on PullRequest" part, even using their InputObject.  Until we
                    // can, we'll use annotations to represent the response format while passing the query in as a string.
                    //.request(PrsAssignedToMeQuery.class)
                    .build();
            // System.err.println("req=" + request);
            GraphQLResponseEntity<PrsAssignedToMeQuery> response = new GraphQLTemplate().query(request,PrsAssignedToMeQuery.class);
            if (response.getErrors() == null) {
                List<PullRequest> output = new ArrayList<>();
                for (Edge pr : response.getResponse().search.edges) {
                    output.add(new PullRequest(pr.node.number, pr.node.title, pr.node.url));
                }
                return output;
            } else {
                throw new GraphQLException(Arrays.asList(response.getErrors()).toString());
            }
        }
        catch (GraphQLException e) {
            // As a special case, we sometimes get exceptions that look exactly like this:
            //     GraphQLException{message='null', status='null', description='api.github.com', errors=null}
            // Since this doesn't tell us much, let's detect this case and log the stacktrace at least before
            // re-throwing.
            if (
                    e.getMessage() == null && e.getStatus() == null && e.getErrors() == null
                    && (e.getDescription() == null || apiHostName.equals(e.getDescription()))
               ) {
                // We could wrap this stacktrace into the message or description instead of using stderr.
                e.printStackTrace();
            }
            throw e;
        }
    }

//    protected void getPullRequestsAssignedToMe() throws IOException {
//        HttpClient client = HttpClients.createDefault();
//        HttpPost request = new HttpPost("https://api.github.com/graphql");
//        request.setHeader("Authorization", "bearer " + apiKey);
//        StringEntity entity = new StringEntity(
//                "{ \"query\": \"" +
//                        "query { " +
//                        "search(query:\\\"assignee:jonathanstokes is:pr is:open\\\", type:ISSUE,last:100) { " +
//                        "edges { " +
//                        "node { " +
//                        "... on PullRequest { " +
//                        "title " +
//                        "url " +
//                        "assignees(last:100) { " +
//                        "edges { " +
//                        "node { " +
//                        "login " +
//                        "} " +
//                        "} " +
//                        "} " +
//                        "} " +
//                        "} " +
//                        "} " +
//                        "} " +
//                        "}\"" +
//                        "}"
//        );
//        System.err.println("req=" + IOUtils.toString(entity.getContent()));
//        entity.setChunked(true);
//        request.setEntity(entity);
//        HttpResponse response = client.execute(request);
//        String responseData = IOUtils.toString(response.getEntity().getContent());
//        System.err.println("resp=" + responseData);
//    }
//
//    protected void getPullRequestsICreated() throws IOException {
//        HttpClient client = HttpClients.createDefault();
//        HttpPost request = new HttpPost("https://api.github.com/graphql");
//        request.setHeader("Authorization", "bearer " + apiKey);
////  { \"query\": \"\
////    query { \
////        user(login: "jonathanstokes") { \
////            pullRequests(first: 10, states: OPEN) { \
////                totalCount \
////                nodes { \
////                createdAt \
////                number \
////                title \
////                assignees(first: 100) { \
////                    nodes { \
////                    name \
////                    } \
////                } \
////                } \
////                pageInfo { \
////                hasNextPage \
////                endCursor \
////                } \
////            } \
////        } \
////    }\" \
//// } \
//        StringEntity entity = new StringEntity(
//                "{ \"query\": \"" +
//                        "query { " +
//                        "user(login: \\\"jonathanstokes\\\") { " +
//                        "pullRequests(first: 10, states: OPEN) { " +
//                        "totalCount " +
//                        "nodes { " +
//                        "createdAt " +
//                        "number " +
//                        "title " +
//                        "assignees(first: 100) { " +
//                        "nodes { " +
//                        "name " +
//                        "} " +
//                        "} " +
//                        "} " +
//                        "pageInfo { " +
//                        "hasNextPage " +
//                        "endCursor " +
//                        "} " +
//                        "} " +
//                        "} " +
//                        "}\"" +
//                        "}"
//        );
//        System.err.println("req=" + IOUtils.toString(entity.getContent()));
//        entity.setChunked(true);
//        request.setEntity(entity);
//        HttpResponse response = client.execute(request);
//        String responseData = IOUtils.toString(response.getEntity().getContent());
//        System.err.println("resp=" + responseData);
//    }

}
