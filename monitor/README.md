# Monitor

## Deployment

```
$ heroku addons:add mongolab --app remon-client
$ heroku git:remote --app remon-client
$ git push heroku `git subtree split --prefix monitor <current_branch>`:master --force
```
