

function getAssociatedData(reqName, data)
{
	var jsonrpc = new JSONRpcClient("JSON-RPC");
	var result = jsonrpc.JSONMethods.getAssociatedData(reqName.name, data);
	
	var selectElement = document.getElementById(reqName.name);
	while(selectElement.length >= 1)
		selectElement.remove(selectElement.length-1);
	for(var i = 0; i < result.list.length; i++)
	{
		selectElement.options[selectElement.options.length] = new Option(result.list[i], result.list[i]);
	}
}
