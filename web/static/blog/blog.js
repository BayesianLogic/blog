CodeMirror.defineMode("diff", function() {

	var keywords = wordRegexp(['extern','import','fixed','func','distinct','random','origin','param','type', 'obs', 'query']);
	var types = wordRegexp(['Int','Real','Boolean','NaturalNum','List','Map','TabularCPD','Categorical','Distribution','Gaussian','type0','type1','type2','typename']);

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
