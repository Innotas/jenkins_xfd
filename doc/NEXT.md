

### Get all the PRs assigned to me

    query {
        search(query:"assignee:jonathanstokes is:pr is:open", type:ISSUE,last:100) {
            edges {
            node {
                ... on PullRequest {
                title
                url
                assignees(last:100) {
                    edges {
                    node {
                        login
                    }
                    }
                }
                }
            }
            }
        }
    }

### Get all the PRs I created

    query {
        user(login: "jonathanstokes") {
            pullRequests(first: 10, states: OPEN) {
                totalCount
                nodes {
                createdAt
                number
                title
                assignees(first: 100) {
                    nodes {
                    name
                    }
                }
                }
                pageInfo {
                hasNextPage
                endCursor
                }
            }
        }
    }

