CodeMirror.defineMode("diff", function() {

	var keywords = wordRegexp(['extern','import','fixed','func','distinct','random','origin','param','type', 'obs', 'query']);
	var types = wordRegexp(['Integer', 'Real', 'Boolean', 'NaturalNum', 'String', 'List', 'Map', 'TabularCPD','Categorical']);

	function wordRegexp(words) {
        return new RegExp("^((" + words.join(")|(") + "))\\b");
    }
	
  	return {
  		token: function(stream){
  			
  			console.log(stream.current());
  			// Remove all space
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
