$(function() {
  document.getElementById('viewres').style.visibility='hidden';
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

 editor = CodeMirror.fromTextArea($('#textfield')[0], {

    lineNumbers: true,
    matchBrackets: true
  });

 num_res=0;
//editor.setOption("theme", "railscasts");
  $(".button").click(function() {
    var input_string = editor.getValue();
    var samp_base = $('input[name=samp_base]:checked').val();
    var samp_eng = $('input[name=samp_eng]:checked').val();
    var samp_alg = $('input[name=samp_alg]:checked').val();
    var input_data = {"textfield" : input_string, "base" : samp_base, "eng" : samp_eng, "alg" : samp_alg};
    re = /@/i;
    var found = input_string.match(re);
    if(found==null&&samp_eng=="ParticleFilter"){
      alert(" ParticleFilter can only be used to dynamic models!");
      return false;
    }
    var i;
    for (i=1;i<=num_res;i++ ){
      var chartname='#chardiv'+i.toString();
      $("#chartdiv"+i.toString()).css({"height"		: "0px"});
      //console.log(AmCharts.charts);
      for(key in AmCharts.charts)
        AmCharts.charts[key].clear();
    }
    num_res=0;
    $.ajax({
      type: "POST",
      data: input_data,
      success: function(data) {
        //console.log(data);
        //console.log(eval(data));
        if (data=="error occurred"){
          alert(data);
          return false;
        }
        var real_data= eval(data);

        //console.log(data);
        for (key1 in real_data)
          if(real_data.hasOwnProperty(key1)){
          var samples = real_data[key1]['samples']
          var queries = real_data[key1]['queries']
          for(key in queries){
            if(queries.hasOwnProperty(key)){
              var title = queries[key]['query'];
              var dist = queries[key]['distribution'];
              showchart(samples,title, dist);
            }
          }
        }
        //$('#results').html($(data));
      },
    });
    document.getElementById('viewres').style.visibility='visible';
    return false;
  });
});
function showhide() {
    var x = document.getElementById("usrtext");
    if (x.style.display === "none") {
        x.style.display = "block";
    } else {
        x.style.display = "none";
    }
}
function getval(selbox){
  $.ajax({
    type: "POST",
    data: {sel_file : selbox.value},
    success: function(data) {
      editor.setValue(data);
      //console.log(eval(data));
      //$('#results').html($(data));
    },
  });
}
function handlebase(myradio){
//  $("#sampno").slider("destroy");
  $("#sampno").prop({"max":parseInt(myradio.value)*2});
  $("#sampno").val(parseInt(myradio.value));
var output = document.getElementById("demo");
output.innerHTML = slider.value;
}
function noth(){

}
function showchart(samples,title, dist, num){
  num_res=num_res+1;
  var newchart='<div class="chartdiv" id="chardiv'+num_res.toString()+'"></div> ';
//  $(".modal-body").append(newchart);
$("#chartdiv"+num_res.toString()).css({"height"		: "300px"});
//  console.log("chartdiv"+num_res.toString());
var chart = AmCharts.makeChart("chartdiv"+num_res.toString(), {
  "type": "serial",
  "theme": "light",
  "titles": [
		{
			"text": title+" w/ "+samples+" samples",
			"size": 15
		}
	],
  "dataProvider": dist,
  "gridAboveGraphs": true,
  "startDuration": 1,
  "graphs": [ {
    "balloonText": "[[category]]: <b>[[value]]</b>",
    "fillAlphas": 0.8,
    "lineAlpha": 0.2,
    "type": "column",
    "valueField": "probability"
  } ],
  "chartCursor": {
    "categoryBalloonEnabled": false,
    "cursorAlpha": 0,
    "zoomable": false
  },
  "categoryField": "value",
  "categoryAxis": {
    "gridPosition": "start",
    "gridAlpha": 0,
    "tickPosition": "start",
    "tickLength": 20
  },
  "export": {
    "enabled": true
  }
});
}
var slider = document.getElementById("sampno");
var output = document.getElementById("demo");
output.innerHTML = slider.value;

slider.oninput = function() {
  output.innerHTML = this.value;
}
