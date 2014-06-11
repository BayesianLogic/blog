title: Developer's Guide
date: 2014-06-11 15:11

The BLOG project is hosted on Github. Contributions in the form of GitHub pull requests. 

# General guideline
- Break your work into small, single-purpose patches if possible. It’s much harder to merge in a large change with a lot of disjoint features. Make your branch **small** enough that only contains one single feature change, multiple features should be completed in seperate branch and pull request.
- Create an issue if you discover some bug or propose a new feature. Put as much label as possible.
- Do not push commit directly to master. Instead, submit the patch as a GitHub pull request. For a tutorial, see the GitHub guides on forking a repo and sending a pull request. 
- When creating a pull request, please link to an issue if it already exists. 
- Follow the Code Style Guide. Before sending in your pull request, run sbt/sbt scalastyle to validate the style.
- Large files (e.g. data files > 10MB, any output file, generated pdf etc) should not be added to git repo.
- Update the documentation (in the docs folder) if you add a new feature or commandline option.
 

# Code style
Use [google java style](http://google-styleguide.googlecode.com/svn/trunk/javaguide.html)
 * 2 spaces for indent
 * no long lines
 * Please use \n for line terminator! 

# Eclipse
- Code style: Eclipse config file is provided. Please use the version already in git master branch.

- Running a BLOG model:
  1. Enter `Run Configurations`, create a new configuration
  2. Set `project` to outbids
  3. Set `Main class` to `blog.Main`
  4. In the `Arguments` tab, pass in the path to the BLOG model, and any parameters

# Generating Lexer and Parser
You only need to do this if you modified `BLOGLexer.flex` or `BLOGParser.cup`
```
  make parser
```

# Compiling source
- Under Linux/MAC OSX
```
  make compile
```
- Under Windows
```
  compile.bat
```

# Package
To make a release
```
  make zip
```

# Git Basics
You may also be interested in some Git basics: 

https://github.com/lileicc/dblog/wiki/git-workflow

## Pull Request

The basic unit of code review is a pull request. Here is how you make one. Note you can PR off branches, so there is no need to fork the repo. This is what's referred to as "Shared Repository Model" in the doc below. 

https://help.github.com/articles/using-pull-requests

## How to use Pull Requests

You should create a _feature branch_ whenever you start to work on something. This is the branch you will work off for the entire development cycle. When you finish development on the feature, you will merge this branch back into master. This way, we can ensure that our master branch is always in a good state, and never has anything that is only partially complete.

``` 
$ git checkout -b feature_name 
```

As early as you can (you can even do this before you start _any_ work) start a pull request on Github. You should use the PR for discussion about this particular feature as well. Periodically push your code to this branch, which will update the PR. It's recommended that you get your code reviewed at regular intervals rather than all at once.

## Managing Feature Branches & Pull Requests

### Squashing Commits

The git motto is that you should commit often. Some people go as far as committing every 5-10 minutes. This is perfectly fine, and recommended for your _local_ history while you work. It allows you to revert easily fi you make a mistake. 

However, all these granular commits may not be the easiest thing for a reviewer to address (b/c each commit shows up as a separate page you have to click on and comment). Therefore, you should commit often but [squash your commits](https://ariejan.net/2011/07/05/git-squash-your-latests-commits-into-one) before you push to the PR. 

### Updating Your Branch

For long features, your feature branch may fall behind of master a lot. This is bad, since it will introduce difficult merge issues later on (the more you fall behind of master, the harder it is for you to merge your changes back into it). Therefore, you will want to regularly update your feature branch with commits that happened on the master branch. 

The desired operation here is a _rebase_ rather than a merge. A rebase will take the history seen on master, and replay it as if they happened on your branch. This has the effect of keeping the history clean, as the only "merge" entries we will see are when we merge feature branches back into master. 

The commands you should run to rebase are

```
git fetch --all
git rebase origin/master
```

When you do push your code after a rebase, you will have to do so with

```
git push --force
```

The `--force` option is required since a rebase re-writes revision history, so your local history and the upstream history will disagree. `--force` tells git you want to override the upstream history with the rebased history.

## Closing a Pull Request
When you get an `LTGM`, just click the merge button on github to merge your changes into master.
