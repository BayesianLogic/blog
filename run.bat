set BLOG_HOME=.

java -cp "%BLOG_HOME%/bin;*;%BLOG_HOME%/lib/*" -Xmx2048M blog.Main %*
