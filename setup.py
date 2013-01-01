#!/usr/bin/python

from setuptools import setup 

entry_points = """ 
[pygments.lexers] 
blog = blog.lexer:BlogLexer
""" 

setup( 
    name         = 'blog', 
    version      = '0.5', 
    author       = "Sharad Vikram, Lei Li", 
    packages     = ['blog'], 
    entry_points = entry_points 
) 
