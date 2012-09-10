from pygments.lexer import RegexLexer, bygroups, include
from pygments.token import *

class BlogLexer(RegexLexer):
    name = 'BLOG'
    aliases = ['blog']
    filenames = ['*.blog']
    operators = ['=','~',':']
    keywords = ['import','fixed','func','distinct','random','origin']
    types = ['Int','Real','Boolean','NaturalNum','List','Map','TabularCPD','Categorical','Distribution','Gaussian','type','type0','type1']
    type_delims = ["\[","\]","<",">"]
    operator_regex = "|".join(operators)
    keyword_regex = "|".join(keywords)
    type_regex = "|".join(types)
    delim_regex = "|".join(type_delims)

    tokens = {
        'comments': [
            (r'/\*.*?\*/', Comment),
            (r'//.*?\n', Comment),
        ],
        'root': [
            include('comments'),
            (r'\s+', Text),
            (r''+type_regex,Keyword.Type),
            (r'('+keyword_regex+')(\s+)',
              bygroups(Keyword.Declaration,Text)),
            (r'('+type_regex+')(\s*)',
              bygroups(Keyword.Type,Text)),
            (r'\s*('+delim_regex+')\s*(.*?)\s*('+delim_regex+')\s*',
              bygroups(Text,Name.Variable,Text)),
            (r'(.*?)\s*('+operator_regex+')(\s*)(.*?)$', 
              bygroups(Name.Variable, Operator, Text, Text)),
            (r'('+operator_regex+')(\s*)(.*?)$', 
              bygroups(Operator, Text, Text)),
            (r'('+type_regex+')(\s*)(\()',
              bygroups(Name.Variable,Text,Text)),
            (r'(.*?)(\s*)(\()',
              bygroups(Name.Variable,Text,Text),'params'),
        ],
        'params' : [
            (r'('+type_regex+')(\s+)([a-zA-Z]+[0-9]*)([,]?[ ]*)', bygroups(Keyword.Type,Other,Name.Variable,Text)),
            (r'\)(\s*)',Text,"#pop"),
        ],
    }
