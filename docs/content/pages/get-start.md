title: Get-started
status: hidden
date: 2014-06-11 15:11
sortorder: 15

# Downloading and Installing
Get the latest version of BLOG from the 
[download page](http://bayesianlogic.cs.berkeley.edu/download). 

## Prerequsite
- Java 1.6+
- all operating systems. (Windows XP/7/8, MacOSX, Linux)

## Installing
Running BLOG doesnot require install. However, you may use the following script to 
install it on your system. It will be more convenient to run BLOG.

Alternatively, you may directly compile from source code by. 

    make compile

<!-- *** Placeholder for installation *** -->

# Running BLOG
Open your terminal and locate to your BLOG main directory.
The BLOG includes a few examples. 

On Linux/Mac, the following command will run the Burglary example. 
```
./blog example/burglary.blog
```
If you already installed BLOG, you may try without `./`
```
blog example/burglary.blog
```

On Window, the corresponding command is (You may need to properly set your JAVA_HOME and PATH environment variable for Java.)
```
blog.bat example/burglary.blog
```

The general command is 
```
./blog <filename_of_blog_model>
```

BLOG system accepts a list of commandline options, such as the number samples
used in its underlying sampling engine. The more samples, the more accurate
result it can produce. Use the following to run Burglary example with 1 million 
samples. 
```
./blog -n 1000000 example/burglary.blog
```

The following will run a simple Hidden Markov Model for genetic sequences. 
It uses particle filtering algorithm to make inference. 
```
./dblog example/hmm.dblog
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
