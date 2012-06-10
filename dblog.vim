" Vim Syntax File
" Language: dblog
" Maintainer: Bharath Ramsundar
" Latest Revision: 6/9/2012

if exists("b:current_syntax")
    finish
endif

" Keywords
syn keyword dblogKeywords type distinct random origin fixed param
syn keyword dblogType Real
syn keyword dblogStatement if

" Matches
syn match dblogComment "//.*$"
syn match dblogNumberStatement "^\s*#\w\+"

" Region
syn region dblogBlockComment start="^\s*/\*" end="\*/\s*$"

let b:current_syntax = "dblog"

hi def link dblogComment Comment
hi def link dblogBlockComment Comment
hi def link dblogKeywords PreProc
hi def link dblogNumberStatement Type
hi def link dblogStatement Statement
