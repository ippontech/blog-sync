# Blog Sync

This contains code to synchronize our Ghost blog with Git.

## GitHub Webhook - Git to Ghost on updates

A WebHook is defined on GitHub to upload new or updated posts to Ghost:

* This only works on the `master` branch: anything committed to another branch will be ignored.
* If a Pull Request is merged onto `master`, Ghost should be updated within 30 seconds.

This relies on some AWS infrastructure:

* GitHub calls an API Gateway endpoint.
* The API Gateway endpoint triggers a Lambda.
* The Lambda executes `com.ippontech.blog.import.LambdaHandler`.

## Bulk import from Git to Ghost

To synchronize Ghost from Git in bulk, you can use the `com.ippontech.blog.import.LocalGitRepoToGhostKt` entry point.

You will need to specify the location to a local checkout of the Git repository.

You can then edit the `main` function to either update all the posts or only a few ones.

## Bulk export from Ghost to Git

To export all the posts from Ghost to Git, you can use the `com.ippontech.blog.export.GhostToGitKt` entry point.

You will need to specify the location to a local checkout of the Git repository where to export to.

Once the export is complete, you will need to commit & push to Git. Make sure you have a clean checkout prior to running the export.

## Environment variables

All the entry points require 4 environment variables to be specified:

* GHOST_USERNAME
* GHOST_PASSWORD
* GHOST_CLIENT_ID
* GHOST_CLIENT_SECRET
