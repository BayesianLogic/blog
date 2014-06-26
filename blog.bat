set BLOG_HOME=.

java -cp "%BLOG_HOME%/bin;*;%BLOG_HOME%/lib/java-cup-11b.jar;%BLOG_HOME%/lib/*" -Xmx2048M blog.Main %*
