//  For Drawing a arrow from point A to point B
//	http://stuff.titus-c.ch/arrow.html
function canvas_arrow(context, fromx, fromy, tox, toy, sentiments, density ){
		var count = sentiments.length;

		dist = Math.sqrt((fromx-tox)*(fromx-tox)+(fromy-toy)*(fromy-toy));
		disTrunc = nodeAvgSize/2;
		if(dist<1)
			dist = 1;
		ratioTrunc = disTrunc/dist;
		//Shorter line by 10%
		ax = ratioTrunc*fromx + (1-ratioTrunc)*tox;
		ay = ratioTrunc*fromy + (1-ratioTrunc)*toy;
		bx = (1-ratioTrunc)*fromx + ratioTrunc*tox;
		by = (1-ratioTrunc)*fromy + ratioTrunc*toy;
		fromx = ax;
		fromy = ay;
		tox = bx;
		toy = by;

	  	//drawArrow(context, fromx, fromy, tox, toy,0);
        

		var headlen = 10;	// length of head in pixels
		var dx = tox-fromx;
		var dy = toy-fromy;
		var angle = Math.atan2(dy,dx);
		var anglePendicular = angle + Math.PI/2;
		var middleX = (fromx + tox)/2;
		var middleY = (fromy + toy)/2;
		var ppdThickness = count/4;
		

		for (var i = 0; i < count; i++) {

			var senti = sentiments[i];
			if(senti>0)
				context.strokeStyle = 'rgb(0,' +
                       Math.floor(127+senti*127) + ',0)';
			else if(senti<0)
				context.strokeStyle = 'rgb('+ Math.floor(127-senti*127)+',0,0)';
			else context.strokeStyle = 'black';
			context.moveTo(fromx, fromy);
			var ptx = middleX+Math.cos(anglePendicular)*ppdThickness*(count-i*2);
			var pty = middleY+Math.sin(anglePendicular)*ppdThickness*(count-i*2);
			context.quadraticCurveTo(ptx, pty, tox, toy);
			//context.lineTo(ptx, pty);
		}
		context.lineTo(tox, toy);
		context.lineTo(tox-headlen*Math.cos(angle-Math.PI/6),toy-headlen*Math.sin(angle-Math.PI/6));
		context.moveTo(tox, toy);
		context.lineTo(tox-headlen*Math.cos(angle+Math.PI/6),toy-headlen*Math.sin(angle+Math.PI/6));
}


/*** Sort The Fields and then Push Keys and Data to HTML ***/
var dataProperties = "";
var dataMap;

//For Sorting According to Keys
var hashKeys = ['Subject','Date','Topic','Sentiment','EpochTimestamp','Message_ID','Content',
				'Name','Email'];
var indexes = [];

function showProperties(data) {
	indexes = [];
	dataProperties = "";
	dataMap = data;
	Object.keys(dataMap).forEach(appendIndex);
	indexes.sort();

	//Now Pushing Data to Div
	for (var i = 0; i < indexes.length; i++) {
		appendProperites(indexes[i][1]);
	}
	document.getElementById('properties').innerHTML = dataProperties;
	//Free Memory
	dataMap = null;
}

function appendIndex(key) {
	indexes.push([getId(key),key]);
}
function appendProperites(key) {
	if(dataMap[key]=="")
		return;
	dataProperties += "<div class='card'><strong>"+key+"</strong>";
	dataProperties += "<article>"+dataMap[key]+"</article></div>";
}

function getId(key) {
	for (var i = 0; i < hashKeys.length; i++) {
		if(key == hashKeys[i])
			return i;
	}
	return hashKeys.length;
}