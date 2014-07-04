title: Get-started
status: hidden
date: 2014-07-01 15:11
sortorder: 15

# Downloading and Installing
Get the latest version of BLOG from the 
[download page]({filename}download.md). 

## Prerequsite
- Java 1.6+
- all operating systems. (Windows XP/7/8, MacOSX, Linux)

## Installing
You may choose to install the pre-built version for your operating system. 
You may also running BLOG from the universal package, which doesnot require install. 

Alternatively, you may directly compile from source code by 
(you will need sbt installed, check [download page]({filename}develop-guide.md) for more details). 

    sbt/sbt compile
    sbt/sbt stage

<!-- *** Placeholder for installation *** -->

# Running BLOG
Open your terminal and locate to your BLOG main directory.
The BLOG includes a few examples. 

On Linux/Mac, the following command will run the Burglary example. 
```
bin/blog example/burglary.blog
```
If you already installed BLOG, you may try without `bin/`
```
blog example/burglary.blog
```

On Window, the corresponding command is (You may need to properly set your JAVA_HOME and PATH environment variable for Java.)
```
blog.bat example/burglary.blog
```

The general command is 
```
blog <filename_of_blog_model>
```

BLOG system accepts a list of commandline options, such as the number samples
used in its underlying sampling engine. The more samples, the more accurate
result it can produce. Use the following to run Burglary example with 1 million 
samples. 
```
blog -n 1000000 example/burglary.blog
```

The following will run a simple Hidden Markov Model for genetic sequences. 
It uses particle filtering algorithm to make inference. 
```
dblog example/hmm.dblog
```

The full list of commandline options are described in [BLOG User Manual]({filename}user-manual.md)

# Where to go from here
- [User manual]({filename}user-manual.md): a manual for BLOG system, including setting and tuning all options. 
- [BLOG language reference](xxxxx): a reference for BLOG language, including its syntax and semantics. 
- [Probabilistic modelling using BLOG](xxx): a tutorial textbook for modelling using BLOG. It includes introduction to BLOG language, and how to use it to solve real application problems. 
- [Developer guide]({filename}develop-guide.md): how to contribute to the BLOG project. 

# Community
You may subscribe to the [Mailing list for BLOG users](xxx).
If you want to contribute to BLOG, please refer to [Developer guide]({filename}develop-guide.md).
