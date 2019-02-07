package com.teratory.xfd.github.model.query;

import io.aexp.nodes.graphql.annotations.GraphQLProperty;

@GraphQLProperty(name="... on PullRequest")
public class OnPullRequest {

    public String title;


    @Override
    public String toString() {
        return "{ ... on PullRequest }";
    }
}
