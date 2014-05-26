$(function() {
  CodeMirror.defineMode("diff", function() {
    var keywords = wordRegexp(['extern','import','fixed','distinct','random','origin','param','type', 'obs', 'query', 'for', 'forall','exists', 'if', 'then', 'else', 'null']);
    var types = wordRegexp(['Integer', 'Real', 'Boolean', 'NaturalNum', 'String', 'List', 'Map', 'RealMatrix', 'IntMatrix', 'TabularCPD','Categorical']);

    function wordRegexp(words) {
      return new RegExp("^((" + words.join(")|(") + "))\\b");
    }

    return {
      token: function(stream){
        if (stream.eatSpace()) return null;
        if (stream.match(keywords)) return 'keyword';
        if (stream.match(types)) return 'tag';
        if (stream.match(/[:=~]/)) return 'atom';
        stream.next();
        return null;
      }
    };
  });

  CodeMirror.defineMIME("text/x-diff", "diff");

  var editor = CodeMirror.fromTextArea($('#textfield')[0], {
    lineNumbers: true,
    matchBrackets: true
  });

  $(".button").click(function() {
    var input_string = editor.getValue();
    $.ajax({
      type: "POST",
      data: {textfield : input_string},
      success: function(data) {
        $('#results').html($(data));
      },
    });
    return false;
  });
});
