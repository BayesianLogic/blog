" Vim Syntax File
" Language: blog
" Maintainer: Bharath Ramsundar, Lei Li
" Latest Revision: blog 0.7

if exists("b:current_syntax")
    finish
endif

" Keywords
syn keyword blogKeywords type distinct random origin fixed param query obs
syn keyword blogType Real Timestep Boolean Integer 
syn keyword blogStatement if then else null true false forall exists for
syn keyword blogDistribution Bernoulli BooleanDistrib Categorical CharDistrib Dirichlet EqualsCPD Gamma Gaussian Geometric Iota LinearGaussian MixtureDistrib MultivarGaussian NegativeBinomial Poisson RoundedLogNormal TabularCPD UniformChoice UniformInt UniformVector UniformReal UnivarGaussian
" this one was used in old syntax
syn keyword blogKeywords guaranteed nonrandom
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
