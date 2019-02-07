package com.teratory.xfd.github.model.query.assignedprs;

/**
 * search(query:\\\"assignee:jonathanstokes is:pr is:open\\\", type:ISSUE,last:100) { \
 *             edges { \
 *                 node { \
 *                     ... on PullRequest { \
 *                         title \
 *                         url \
 *                         assignees(last:100) { \
 *                             edges { \
 *                                 node { \
 *                                     login \
 *                                 } \
 *                             } \
 *                         } \
 *                     } \
 *                 } \
 *             } \
 *         } \
 */
public class Search {

    public Edge[] edges;
}
