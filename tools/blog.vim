" Vim Syntax File
" Language: blog
" Maintainer: Bharath Ramsundar, Lei Li
" Latest Revision: blog 0.6

if exists("b:current_syntax")
    finish
endif

" Keywords
syn keyword blogKeywords type distinct random origin fixed param query obs
syn keyword blogKeywords guaranteed
syn keyword blogType Real R3Vector Timestep Boolean Integer 
syn keyword blogStatement if then else null true false forall exists for
syn keyword blogDistribution TabularCPD Dirichlet UniformVector UniformReal
syn keyword blogDistribution MultivarGaussian Poisson UnivarGaussian Gaussian
syn keyword blogDistribution Categorical UniformChoice Bernoulli Geometric

" Matches
syn match blogComment "//.*$"
syn match blogNumberStatement "^\s*#\w\+"
syn match blogNumberStatement "#{[^\{}]*}"
syn match blogListOrArrayStement "\[.*\]"
syn match blogTimeStatement "@\d*"
syn match blogSetStatement "{[^{}]*}"
syn region blogBlockComment start="^\s*/\*" end="\*/\s*$"

let b:current_syntax = "blog"

hi def link blogComment Comment
hi def link blogBlockComment Comment
hi def link blogKeywords PreProc
hi def link blogNumberStatement Constant
hi def link blogType Type
hi def link blogStatement Statement
hi def link blogDistribution Type
hi def link blogSetStatement Constant
hi def link blogTimeStatement Constant
