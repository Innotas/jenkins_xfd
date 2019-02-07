

### v4 Explorer

- [Explorer](https://developer.github.com/v4/explorer/)
- [API Docs](https://developer.github.com/v4/object/pullrequest/)
- [Personal Access Tokens](https://github.com/settings/tokens)

### v4 connection

curl -H "Authorization: bearer my-access-token" -X POST -d " \
 { \
   \"query\": \"query { viewer { login }}\" \
 } \
" https://api.github.com/graphql

Outputs:
```json
{"data":{"viewer":{"login":"jonathanstokes"}}}
```


### Get all the PRs assigned to me

curl -H "Authorization: bearer my-access-token" -X POST -d " \
 { \"query\": \"\
    query { \
        search(query:\\\"assignee:jonathanstokes is:pr is:open\\\", type:ISSUE,last:100) { \
            edges { \
                node { \
                    ... on PullRequest { \
                        title \
                        url \
                        assignees(last:100) { \
                            edges { \
                                node { \
                                    login \
                                } \
                            } \
                        } \
                    } \
                } \
            } \
        } \
    }\" \
 } \
" https://api.github.com/graphql

Outputs:
```json
{
  "data": {
    "search": { 
      "edges": [
        {
          "node": {
            "title": "Fix pdf overflow on mobile",
            "url":"https://github.com/casetext/maat/pull/2263",
            "assignees": {
              "edges": [
                {
                  "node": {
                    "login":"jonathanstokes"
                  }
                }
              ]
            }
          }
        }
      ]
    }
  }
}
```


### Get all the PRs I created

curl -H "Authorization: bearer my-access-token" -X POST -d " \
 { \"query\": \"\
    query { \
        user(login: "jonathanstokes") { \
            pullRequests(first: 10, states: OPEN) { \
                totalCount \
                nodes { \
                createdAt \
                number \
                title \
                assignees(first: 100) { \
                    nodes { \
                    name \
                    } \
                } \
                } \
                pageInfo { \
                hasNextPage \
                endCursor \
                } \
            } \
        } \
    }\" \
 } \
" https://api.github.com/graphql

Output
```json
{
  "data": {
    "user": {
      "pullRequests": {
        "totalCount": 1,
        "nodes": [
          {
            "createdAt": "2018-10-30T22:19:47Z",
            "number": 1797,
            "title": "FeatureGuardService idea",
            "assignees": {
              "nodes": [
                {
                  "name": null
                }
              ]
            }
          }
        ],
        "pageInfo": {
          "hasNextPage": false,
          "endCursor": "Y3Vyc29yOnYyOpHODYlHWg=="
        }
      }
    }
  }
}
```

