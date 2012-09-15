from pygments.lexer import RegexLexer, bygroups, include
from pygments.token import *

class BlogLexer(RegexLexer):
    name = 'BLOG'
    aliases = ['blog']
    filenames = ['*.blog']
    operators = ['=','~',':']
    keywords = ['extern','import','fixed','func','distinct','random','origin','param','type']
    types = ['Int','Real','Boolean','NaturalNum','List','Map','TabularCPD','Categorical','Distribution','Gaussian','type0','type1','type2','typename']

    def gen_regex(ops):
        return "|".join(ops)

    tokens = {
        'arithmetic' : [
            (r'[{}\[\]<>,;\.+*/%&|-]', Text),
            (r'[0-9]+\.[0-9]+', Token.Literal.Number),
            (r'[0-9]', Token.Literal.Number),
         ],
        'variable' : [
            (r'[a-zA-Z_\-]*?[0-9_-]*?[^a-zA-Z]', Name.Variable),
        ],
        'root' : [
            (r'([a-zA-Z]+[0-9]*)(\()(.*?)(\))', bygroups(Name.Function, Token.Punctuation, Text.Name, Token.Punctuation)),
            (r'('+gen_regex(types)+')([ <>\[\]]?)', bygroups(Name.Class, Text)),
            (r'('+gen_regex(keywords)+')', Token.Keyword),
            (r''+gen_regex(operators)+'', Token.Operator, 'expression'),
            include('variable'),
            #(r'\s+(.*?)(\()(.*?)(\))', bygroups(Text.Name, Token.Punctuation, Text.Name, Token.Punctuation)),
            (r'\s+', Text),
            include('arithmetic'),
        ],
        'expression' : [
            (r';',Text, "#pop"),
            (r'(\s*)(.*?)(\()(.*?)(\))', bygroups(Text, Name.Function, Token.Punctuation, Text.Name, Token.Punctuation)),
            include('variable'),
            include('arithmetic'),
            (r'\s+', Text),
        ]
    }

def run_tests():
    tests = [
        "type Person;",
        "random Real x ~ Gaussian(0, 1);",
        "fixed type0 funcname(type1)= e;",
        "random NaturalNum x~ Poisson(a);",
        "fixed type name = nonrandom - expression;",
        "param Real a: 0 < a & a < 10 ;"
        "distinct type name1, name2, name3;",
        "random Real funcname(type1 x);",
        "1.0 + 2.0 * 3.0",
        "Twice( 10.0 ) * 5.5",
        "fixed NaturalNum[10] c = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];",
        "fixed NaturalNum[2][3] table = [1, 2, 3; 4, 5, 6];",
        "fixed List<NaturalNum> a = List(1, 2, 3, 4, 5, 6);",
        "fixed Map<Boolean, Real> map1 = {true -> 0.3, false -> 0.7};",
        "Categorical<Boolean> cpd1 =Categorical({true -> 0.3, false -> 0.7});",
        "List"
    ]
    lexer = BlogLexer()
    for test in tests:
        print(test)
        for token in (lexer.get_tokens(test)):
            print(token)

#run_tests()
