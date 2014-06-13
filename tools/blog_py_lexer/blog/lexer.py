from pygments.lexer import RegexLexer, bygroups, include
from pygments.token import *

class BlogLexer(RegexLexer):
  name = 'BLOG'
  aliases = ['blog']
  filenames = ['*.blog', '*.dblog']
  operators = ['\\-\\>','=','~',':', '\\+', '\\-', '\\*', '/', '\\[', ']', 
         '\\{', '}', '!', '\\<', '\\>', '\\<=', '\\>=', '==', '!=', 
         '&', '\\|', '=\\>', '#', '\\^', '%', '@']
  wordops = ['Prev', 'IsEmptyString', 'Succ', 'Pred',
             'Prev', 'inv', 'det', 'min', 'max', 
             'round', 'transpose', 'sin', 'cos', 'tan',
             'atan2', 'sum', 'vstack', 'eye', 'zeros', 
             'ones', 'toInt', 'toReal', 'diag', 'repmat', 
             'hstack', 'vstack', 'pi']
  deliminators = [',', ';', '\\(', '\\)']
  keywords = ['extern','import','fixed','distinct','random','origin',
        'param','type', 'forall', 'exists', 'obs', 'query', 
        'if', 'then', 'else', 'for']
  types = ['Integer','Real','Boolean','NaturalNum','List','Map',
           'Timestep','RealMatrix','IntegerMatrix']
  distribs = ['TabularCPD', 'Distribution','Gaussian',
             'UniformChoice', 'MultivarGaussian', 'Poisson',
             'Bernoulli', 'BooleanDistrib', 'Binomial', 'Beta', 'BoundedGenometric',
             'Categorical', 'Dirichlet', 'EqualsCPD', 'Gamma', 'Geometric', 'Iota',
             'LinearGaussian', 'MixtureDistrib', 'Multinomial',
             'NegativeBinamial', 'RoundedLogNormal', 'TabularInterp',
             'UniformVector', 'UnivarGaussian', 
             'Exponential', 'UniformInt', 'UniformReal']
  idname_reg = '[a-zA-Z_]\\w*'

  def gen_regex(ops):
    return "|".join(ops)

  tokens = {
    'root' : [
      (r'//.*?\n', Comment.Single),
      (r'(?s)/\*.*?\*/', Comment.Multiline),
      ('('+idname_reg+')(\\()', bygroups(Name.Function, Punctuation)),
      ('('+gen_regex(types)+')\\b', Keyword.Type),
      ('('+gen_regex(distribs)+')\\b', Name.Class),
      ('('+gen_regex(keywords)+')\\b', Keyword),
      (gen_regex(operators), Operator),
      ('(' + gen_regex(wordops) +')\\b', Operator.Word),
      ('(true|false|null)\\b', Keyword.Constant),
      ('('+idname_reg+')\\b', Name),
      (r'"(\\\\|\\"|[^"])*"', String),
      (gen_regex(deliminators), Punctuation),
      (r'\d*\.\d+', Number.Float),
      (r'\d+', Number.Integer),
      (r'\s+', Text),
    ]
  }

def run_tests():
  tests = [
    "type Person;",
    "distinct Person Alice, Bob, P[100];",
    "random Real x1_x2x3 ~ Gaussian(0, 1);\nrandom Real y ~ Gaussian(x, 1);",
    "random type0 funcname(type1 x) =expression;\nrandom type0 funcname(type1 x) dependency-expression;",
    "random NaturalNum x ~ Poisson(a);",
    "param Real a: 0 < a & a < 10 ;"
    "random Real funcname(type1 x);",
    "1.0 + 2.0 * 3.0 - 4.0",
    "Twice( 10.0 ) * 5.5",
    "fixed NaturalNum[] c = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];",
    "fixed NaturalNum[][] table = [1, 2, 3; 4, 5, 6];",
    "fixed List<NaturalNum> a = List(1, 2, 3, 4, 5, 6);",
    "fixed Map<Boolean, Real> map1 = {true -> 0.3, false -> 0.7};",
    "Categorical<Boolean> cpd1 =Categorical({true -> 0.3, false -> 0.7});",
    "List", 
    "/*abc */",
    """
/* Evidence for the Hidden Markov Model.
 */
"""
  ]
  lexer = BlogLexer()
  for test in tests:
    print(test)
    for token in (lexer.get_tokens(test)):
      print(token)

if __name__ == '__main__':
  run_tests()

