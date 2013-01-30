
var xmlHttp
var policyLocationArray;
var policyLogArray;
var policyContentArray;
var indexArray;


/*Functie ce se ocupa de initializarea XmlHttpObject, este apelata de browser la evenimentul "onkeyup"*/
function populatePage()
{
	document.getElementById("policyTree").innerHTML = "";		

	xmlHttp=GetXmlHttpObject();
	if (xmlHttp==null)
  	{
  		alert ("Browser does not support HTTP Request");
  		return;
  	}
	var url="log.txt";
	xmlHttp.onreadystatechange=readFile;
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
	
} 

function readFile()
{
if (xmlHttp.readyState==4 || xmlHttp.readyState=="complete")
 	{ 
		var str = xmlHttp.responseText;
		
		policyLocationArray = new Array();
		policyLogArray = new Array();
		policyContentArray = new Array();
		indexArray = new Array();
		var dataArray = new Array();
		var i, j = 0;
		dataArray = str.split("+");
		for (i = 0; i < dataArray.length-2; i++)
		{
			var data  = dataArray[i];
		
			var pos = data.indexOf("\n");
			if(pos != -1)
			{
				policyLocationArray[j] = data.substring(0, pos);
				separeContentFromLog(data.substring(pos+1, data.length), i)
				indexArray[j] = j;
				j++;
			}			
		}
		var eval_decision = dataArray[dataArray.length-2];

		
        document.getElementById("eval_decision").innerHTML = "<h3>Evaluation Result: <i>"+eval_decision+"</i></h3>";	

		createDroppingTree();
	}
}

function separeContentFromLog(data, index)
{
	var pos;
	pos = data.indexOf(" ");
	if(pos != -1)
	{
		var policyContentSize = data.substring(0, pos)*1;
		policyContentArray[index] = data.substring(pos, pos+policyContentSize);
		policyLogArray[index] = data.substring(pos+policyContentSize+1, data.length);
	}
}

function createDroppingTree()
{	
	var i, j = 0;
	var elemArray = new Array();
	var XUIArray = new Array();
	var policyArray = new Array();
	var rootElem = (policyLocationArray[0].split("/"))[0];
	
	for (i = 0; i < policyLocationArray.length; i++)
	{
		elemArray = policyLocationArray[i].split("/");
		XUIArray[j] = elemArray[1];
		policyArray[j] = elemArray[2];
		j++;
	}
	sortArray(XUIArray);
	
	var treeRepresentation = "<ul id='dhtmlgoodies_tree2' class='dhtmlgoodies_tree'>"+
						"<li id='node0' noDrag='true' noSiblings='true' noDelete='true' noRename='true'><a href='#'>"+rootElem+"</a>";		

	var policyTree = policyTreeRepresentation(XUIArray, policyArray)
		
	treeRepresentation += policyTree+"</li></ul>"
						
	document.getElementById("policyTree").innerHTML = treeRepresentation;
	initiateTree();
}

function initiateTree()
{
	treeObj = new JSDragDropTree();
	treeObj.setTreeId('dhtmlgoodies_tree2');
	treeObj.setMaximumDepth(7);
	treeObj.setMessageMaximumDepthReached('Maximum depth reached'); // If you want to show a message when maximum depth is reached, i.e. on drop.
	treeObj.initTree();
	treeObj.expandAll();
}

function policyTreeRepresentation(XUIArray, policyArray)
{
	var i;
	var start = 0;
	var policyTree = "";
	if (XUIArray.length > 0)
	{
		policyTree = "<ul><li id='0' noDrag='true'><a href='#'>"+XUIArray[0]+"</a>"
		policyTree +="<ul><li id='" + indexArray[0] + "' noDrag='true' onclick='showLog("+ indexArray[0].toString()+",\""+XUIArray[0].toString()+"\",\""+policyArray[indexArray[0]].toString()+"\")'><a href='#'>" + policyArray[indexArray[0]] + "</a></li>"
		start = 1;
	}
	for (i = 1; i < XUIArray.length; i++)
	{
		if(XUIArray[i]==XUIArray[i-1] )
		{
			if(start == 0){		
				policyTree += "<ul>";
				start = 1;
			}
			policyTree += "<li id='" + indexArray[i] + "'noDrag='true' onclick='showLog(" + indexArray[i].toString() +",\""+XUIArray[i].toString()+"\",\""+policyArray[indexArray[i]].toString()+"\")'><a href='#'>" + policyArray[indexArray[i]] + "</a></li>";				
		}
		else
		{
			var policyName = XUIArray[i]+"/"+policyArray[indexArray[i]];			
			policyTree +="</ul></li>";
			policyTree += "<li id='"+i.toString()+"'noDrag='true'><a href='#'>"+XUIArray[i]+"</a>"
			policyTree += "<ul><li id='" + indexArray[i] + "'noDrag='true' onclick='showLog("+ indexArray[i].toString()+",\""+XUIArray[i].toString()+"\",\""+policyArray[indexArray[i]].toString()+"\")'><a href='#'>" + policyArray[indexArray[i]] + "</a></li>";						
		}		
	}
	if (XUIArray.length > 0)
	{
		policyTree +="</ul></li></ul>"
	}
	
	return policyTree
}

function getStyleClass (className)
{
	for (var s = 0; s < document.styleSheets.length; s++)
	{
		if(document.styleSheets[s].rules)
		{
			for (var r = 0; r < document.styleSheets[s].rules.length; r++)
			{
				if (document.styleSheets[s].rules[r].selectorText == '.' + className)
				{
					return document.styleSheets[s].rules[r];
				}
			}
		}
		else if(document.styleSheets[s].cssRules)
		{
			for (var r = 0; r < document.styleSheets[s].cssRules.length; r++)
			{
				if (document.styleSheets[s].cssRules[r].selectorText == '.' + className)
				return document.styleSheets[s].cssRules[r];
			}
		}
	}
	
	return null;
}


function showLog(id, policyXUID, policyName)
{
	//alert("showLog "+id);
	var logginRepresentation;
	var contentRepresentation;
	var nr_rows = parseInt(policyLogArray[id].length/50)+2;
	
	//alert(logginRepresentation);	
	//logginRepresentation = "<textarea id=\"policyLogTextarea\" class=\"policyLog\" rows='30' cols='10' readonly>" + policyLogArray[id] + "<textarea>"
	
    logginRepresentation = "<i>Reason:</i><br><textarea id=\"policyLogTextarea\" class=\"policyLog\" rows=" + nr_rows + " readonly>\n\n" + policyLogArray[id] + "</textarea>";
	contentRepresentation = "<br><br><i>Content of policy "+policyXUID+"/"+policyName+":</i> <textarea rows='30' cols='50' readonly>" + policyContentArray[id] + "</textarea>";
	document.getElementById("policyLog").innerHTML = logginRepresentation;
	document.getElementById("policyContent").innerHTML = contentRepresentation;
	//getStyleClass('policyLog').style.backgroundColor = '#eeeeee';
	document.getElementById("policyLogTd").style.backgroundColor = '#eeeeee';
	document.getElementById("policyLogTextarea").style.backgroundColor = '#eeeeee';
}


function sortArray(XUIArray)
{
	var i;
	var n = XUIArray.length;
	var swapped = true;
	while(swapped)
	{
		swapped = false;
		n = n-1;
		for(i = 0; i < n; i++)
		{
			if(XUIArray[i] > XUIArray[i+1])
			{
				var z;
				z = XUIArray[i];
				XUIArray[i] = XUIArray[i+1];
				XUIArray[i+1] = z;

				z = indexArray[i];
				indexArray[i] = indexArray[i+1];
				indexArray[i+1] = z;
				swapped = true;
			}
		}
	}
}

/*Initializeaza un obiect XmlHttpObject in functie de browser*/
function GetXmlHttpObject()
{
	var xmlHttp=null;
	try
 	{
 		// Firefox, Opera 8.0+, Safari
 		xmlHttp=new XMLHttpRequest();
 	}
	catch (e)
 	{
 		// Internet Explorer
 		try
  		{
  			xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
  		}
 		catch (e)
  		{
  			xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
		}
 	}
	return xmlHttp;
}
