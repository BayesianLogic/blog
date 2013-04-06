FOR BASIC USER
===============================
Compiling/Install
----------------------
Under Linux/MAC OSX
  make compile

Under Windows
  compile.bat

Usage
----------------------
Under Linux/MAC OSX
 run.sh <path_to_blog_file> <params>

Under Windows
 run.bat <path_to_blog_file> <params>

FOR ADVANCED USER and DEVELOPER
===============================
Generating Lexer and Parser
----------------------

You only need to do this if you modified `BLOGLexer.flex` or `BLOGParser.cup`

1. Run `$ ./gen_parser`

Working with Eclipse
--------------------

Instructions for setting up Eclipse for blog:

1. Create `New Java Project` 
2. Point to `Location` to outbids folder
3. Set `src` as source directory
4. Set `bin` as build directory
5. Import all jars in `lib/`
6. Press finish

Running a BLOG model:

1. Enter `Run Configurations`, create a new configuration
2. Set `project` to outbids
3. Set `Main class` to `blog.Main`
4. In the `Arguments` tab, pass in the path to the BLOG model, and any parameters

Compiling Directly
------------------

0. (needed only if you change parser) Generate Lexer and Parser `$ ./gen_parser`
1. Compile the code `$ make compile`
2. Running BLOG models `$ ./run.sh <path_to_model> <params>`

Git Tips
-------------------
1. make Git ignore line ending
 git config --global core.autocrlf true


Readme Updated: April 05, 2013
