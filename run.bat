set BLOG_HOME=.

java -cp "%BLOG_HOME%/bin;%BLOG_HOME%/lib/java_cup.jar;%BLOG_HOME%/lib/JFlex-1.4.3.jar;%BLOG_HOME%/lib/Jama.jar" -Xmx2048M blog.Main %*
