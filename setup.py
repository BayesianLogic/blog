from setuptools import setup 

entry_points = """ 
[pygments.lexers] 
blog = blog.lexer:BlogLexer
""" 

setup( 
    name         = 'blog', 
    version      = '0.1', 
    author       = "Sharad Vikram", 
    packages     = ['blog'], 
    entry_points = entry_points 
) 
