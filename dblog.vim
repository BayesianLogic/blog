" Vim Syntax File
" Language: dblog
" Maintainer: Bharath Ramsundar
" Latest Revision: 6/18/2012

if exists("b:current_syntax")
    finish
endif

" Keywords
syn keyword dblogKeywords type distinct random origin fixed param query obs
syn keyword dblogKeywords guaranteed
syn keyword dblogType Real R3Vector NaturalNum Timestep Boolean
syn keyword dblogStatement if then else null
syn keyword dblogDistribution TabularCPD Dirichlet UniformVector UniformReal
syn keyword dblogDistribution MultivarGaussian Poisson UnivarGaussian Gaussian
syn keyword dblogDistribution Categorical UniformChoice Bernoulli Geometric

" Matches
syn match dblogComment "//.*$"
syn match dblogNumberStatement "^\s*#\w\+"
syn match dblogNumberStatement "#{[^\{}]*}"
syn match dblogListOrArrayStement "\[.*\]"
syn match dblogTimeStatement "@\d*"
syn match dblogSetStatement "{[^{}]*}"
syn region dblogBlockComment start="^\s*/\*" end="\*/\s*$"

let b:current_syntax = "dblog"

hi def link dblogComment Comment
hi def link dblogBlockComment Comment
hi def link dblogKeywords PreProc
hi def link dblogNumberStatement Constant
hi def link dblogType Type
hi def link dblogStatement Statement
hi def link dblogDistribution Type
hi def link dblogSetStatement Constant
hi def link dblogTimeStatement Constant
