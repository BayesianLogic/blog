Getting Code
=====================

1. `cd` into the directory where you want your code to be
2. Run `$ git clone git@github.com:amatsukawa/dblog.git`

Generating Lexer and Parser
====================

You only need to do this if you modified `BLOGLexer.flex` or `BLOGParser.cup`

1. Run `$ ./gen_parser`

Working with Eclipse
=====================

Instructions for setting up Eclipse for outbids:

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
===================

1. Generate Lexer and Parser `$ ./gen_parser`
2. Compile the code `$ make compile`
3. Running BLOG models `$ ./run.sh <path_to_model> <params>`

Git Tips
===================
1. make Git ignore line ending
 git config --global core.autocrlf true


Readme Updated: May 15, 2012
