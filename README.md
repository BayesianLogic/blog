---
title: BLOG overview
---

**Bayesian Logic** (BLOG) is a first-order probabilistic modeling language. It is designed for making inferences about real-world objects that underlie some observed data: for instance, tracking multiple people in a video sequence, or identifying repeated mentions of people and organizations in a set of text documents. BLOG makes it relatively easy to represent uncertainty about the number of underlying objects and the mapping between objects and observations.

# Basic Users
### Requirement
- Java 1.6 or above (1.7 or above preferred)

### Compiling/Install
- Under Linux/MAC OSX
```
  make compile
```
- Under Windows
```
  compile.bat
```

### Usage
- Under Linux/MAC OSX
```
 ./blog [options] <path_to_blog_file> 
```
- Under Windows
```
 blog.bat [options] <path_to_blog_file> 
```

### Files
- `blog` main execution file to run the engine
- `dblog` main execution file to run the engine with particle filter (alternative can use blog)
- `parse.sh` to check the syntax of a blog file (very useful for debugging)
- `release-note.txt` contains all release information and major changes 

### Syntax highligher for editors
- sublime: see instruction under `tools/blog-for-sublime` 
- emacs: see `tools/blog.el`
- vim: see `tools/blog.vim`
- latex pdf: `blog_py_lexer`, requires a python library `pygments`, and a latex package `minted`

# Contributing and Developer
### Please read before developping: [Developping guideline guideline](https://github.com/lileicc/dblog/wiki/Home)

### Working with Eclipse
- Easy and automatic setup: from Eclipse, clone the git repo, import it. The git repo already contains eclipse setting files. 

- Manually setup instructions for setting up Eclipse (no need to do this if you do previous):
  1. Create `New Java Project` 
  2. Point to `Location` to `dblog` folder
  3. Set `src` as source directory
  4. Set `bin` as build directory
  5. Import all jars in `lib/`
  6. Press finish

- Running a BLOG model:
  1. Enter `Run Configurations`, create a new configuration
  2. Set `project` to outbids
  3. Set `Main class` to `blog.Main`
  4. In the `Arguments` tab, pass in the path to the BLOG model, and any parameters

- Formating source code using Eclipse
  1. All settings are already included in the eclipse setting file in git.
  2. For more please refer to [development guideline](https://github.com/lileicc/dblog/wiki/Home)

### Generating Lexer and Parser
You only need to do this if you modified `BLOGLexer.flex` or `BLOGParser.cup`
```
  make parser
```

### Full compilation steps
0. (needed only if you change parser) Generate Lexer and Parser 
```
  make parser
```
1. Compile the code 
```
  make compile
```
2. Running BLOG models 
```
  blog <path_to_model> <params>
```

### Package
```
  make zip
```

### clean temporal files
```
  make clean
```

### Git Tips
1. make Git ignore line ending
 git config --global core.autocrlf true

Readme Updated: May 23, 2014
