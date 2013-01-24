function create_grid(id, url, colNames, colModel, caption, rowtarget, recordtext, beforeRequest)
{
	var args = {
	    url: url,
	    datatype: 'json',
	    mtype: 'GET',
	    colNames: colNames,
	    colModel: colModel,
	    pager: '#' + id + '_pager',
	    rowNum:10,
	    rowList:[10,20,30],
	    sortname: 'name',
	    sortorder: 'desc',
	    viewrecords: true,
	    gridview: true,
	    caption: caption,
	    autoencode: true,
	    autowidth: true,
	    height: "auto",
	  };
	
	if (rowtarget)
	    args["onSelectRow"] = function(id){if(id)window.location.href = rowtarget + '&id=' + id.replace(/_/g, '/');}
	
	if (recordtext)
	{
	    args["recordtext"] = recordtext;
	    args["emptyrecords"] = recordtext;
	}
	
	if (beforeRequest)
		args["beforeRequest"] = beforeRequest;

	$("#" + id).jqGrid(args);
	return '#gview_' + id;
}